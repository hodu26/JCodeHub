package org.jbnu.jdevops.jcodeportallogin.security

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.entity.User
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.jbnu.jdevops.jcodeportallogin.service.RedisService
import org.jbnu.jdevops.jcodeportallogin.service.token.JwtAuthService
import org.jbnu.jdevops.jcodeportallogin.service.token.KeycloakAuthService
import org.jbnu.jdevops.jcodeportallogin.util.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CustomAuthenticationSuccessHandler(
    private val keycloakAuthService: KeycloakAuthService,
    private val jwtAuthService: JwtAuthService,
    private val authorizedClientService: OAuth2AuthorizedClientService,
    private val userRepository: UserRepository,
    @Value("\${front.domain}") private val frontDomain: String,
    private val jwtUtil: JwtUtil,
    private val redisService: RedisService,
) : AuthenticationSuccessHandler {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: org.springframework.security.core.Authentication
    ) {
        // 1. OAuth2AuthenticationToken으로 캐스팅하고, OAuth2AuthorizedClientService를 통해 Keycloak access token을 가져옴.
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

        // 3. DB에서 사용자 조회; 없으면 회원가입 (기본 STUDENT 역할 부여)
        val user = userRepository.findByEmail(email) ?: userRepository.save(
            User(
                email = email,
                role = RoleType.STUDENT,
            )
        )
        var role = user.role

        // 4. OIDC 사용자라면 id_token 추출 (백엔드에서만 관리)
        val oidcUser = oauth2Auth.principal as? OidcUser
        val idTokenValue: String? = oidcUser?.idToken?.tokenValue

        // 백엔드에서 id_token을 관리(클라이언트에는 노출하지 않음) - redis
        if (idTokenValue != null) {
            redisService.storeIdToken(email, idTokenValue)
        }

        // 5. 자체 JWT 토큰 발급
        val jwtToken = jwtAuthService.createToken(email, role)

        // 6. JWT 토큰을 HttpOnly 쿠키로 전송
        response.addCookie(jwtUtil.createJwtCookie("jwt", jwtToken))

        // 7. SecurityContext에도 인증 정보 저장 (옵션)
        val authorities = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
        val authToken = UsernamePasswordAuthenticationToken(email, null, authorities)
        SecurityContextHolder.getContext().authentication = authToken

        // 8. 로그인 성공 후 프론트엔드 URL로 리다이렉트
        response.sendRedirect("$frontDomain/login/success")
    }
}
