package org.jbnu.jdevops.jcodeportallogin.security

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.jbnu.jdevops.jcodeportallogin.service.token.JwtAuthService
import org.jbnu.jdevops.jcodeportallogin.service.token.KeycloakAuthService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CustomAuthenticationSuccessHandler(
    private val keycloakAuthService: KeycloakAuthService,
    private val jwtAuthService: JwtAuthService,
    private val authorizedClientService: OAuth2AuthorizedClientService, // OAuth2AuthorizedClientService 주입
    private val userRepository: UserRepository
) : AuthenticationSuccessHandler {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: org.springframework.security.core.Authentication
    ) {
        // 1. OAuth2AuthenticationToken으로 캐스팅하여, OAuth2AuthorizedClientService를 통해 Keycloak access token을 가져옴.
        val oauth2Auth = authentication as OAuth2AuthenticationToken
        val authorizedClient = authorizedClientService.loadAuthorizedClient<OAuth2AuthorizedClient>(
            oauth2Auth.authorizedClientRegistrationId,
            oauth2Auth.name
        )
        val keycloakToken = authorizedClient?.accessToken?.tokenValue

        if (keycloakToken.isNullOrEmpty() || !keycloakAuthService.validateToken(keycloakToken)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing Keycloak token")
            return
        }

        // 2. Keycloak 토큰에서 클레임 추출 및 이메일 가져오기
        val claims = keycloakAuthService.extractClaims(keycloakToken)
        val email = claims["email"] as? String
            ?: claims["preferred_username"] as? String
            ?: claims["sub"] as? String

        if (email.isNullOrEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing email in Keycloak token")
            return
        }

        // 3. DB에서 사용자의 역할 조회 (UserService를 통해)
        //    만약 DB에 역할이 없다면 기본으로 STUDENT 역할 사용
        val roleFromDb: RoleType? = userRepository.getRoleByEmail(email)
        val role: RoleType = roleFromDb ?: RoleType.STUDENT

        // 4. 자체 JWT 토큰 발급 (JwtAuthService 사용)
        val jwtToken = jwtAuthService.createToken(email, role)

        // 5. 자체 JWT 토큰을 응답 헤더(또는 쿠키 등)로 클라이언트에 전달
        response.setHeader("Authorization", "Bearer $jwtToken")

        // 6. SecurityContext에도 인증 정보 저장 (필요 시)
        val authorities = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
        val authToken = UsernamePasswordAuthenticationToken(email, null, authorities)
        SecurityContextHolder.getContext().authentication = authToken

        // 7. 로그인 성공 후 원하는 페이지로 리다이렉트 (또는 JSON 응답 등으로 처리)
        response.sendRedirect("/login/success")
    }
}
