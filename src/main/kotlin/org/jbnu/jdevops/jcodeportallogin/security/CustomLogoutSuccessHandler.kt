package org.jbnu.jdevops.jcodeportallogin.security

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.service.RedisService
import org.jbnu.jdevops.jcodeportallogin.util.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.TimeUnit

@Component
class CustomLogoutSuccessHandler(
    private val redisService: RedisService,
    @Value("\${keycloak.logout.url}") private val keycloakLogoutUrl: String, // Keycloak 로그아웃 엔드포인트
    @Value("\${front.domain}") private val frontDomain: String, // 프론트엔드 도메인
    private val jwtUtil: JwtUtil
) : LogoutSuccessHandler {

    @Throws(IOException::class, ServletException::class)
    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        // 1. 세션 무효화 및 SecurityContext 클리어
        request.session?.invalidate()
        SecurityContextHolder.clearContext()

        // 2. 자체 JWT 쿠키 추출 (쿠키 이름 "jwt"라고 가정)
        val jwtCookie = request.cookies?.firstOrNull { it.name == "jwt" }
        if (jwtCookie != null) {
            val jwt = jwtCookie.value
            // 3. 자체 JWT 블랙리스트에 저장 (토큰의 TTL을 1시간으로 설정)
            redisService.addToJwtBlacklist(jwt, 1, TimeUnit.HOURS)

            // 4. JWT 쿠키 제거 (maxAge=0)
            response.addCookie(jwtUtil.createExpiredCookie(jwtCookie.name))
        }

        // 5. Redis에서 id_token 조회 및 삭제
        // 여기서는 authentication.principal에서 사용자를 식별할 수 있는 값(예: 이메일)을 가져온다고 가정
        val email = authentication?.principal?.toString()
        var idToken: String? = null
        if (email != null) {
            idToken = redisService.getIdToken(email)
            if (!idToken.isNullOrEmpty()) {
                redisService.deleteIdToken(email)
            }
        }

        // 6. Keycloak 로그아웃 URL 구성: id_token_hint가 있으면 포함
        val redirectUri = "$frontDomain/"
        val keycloakLogoutRedirectUrl = if (!idToken.isNullOrEmpty()) {
            "$keycloakLogoutUrl?id_token_hint=$idToken&redirect_url=$redirectUri"
        } else {
            "$keycloakLogoutUrl?redirect_url=$redirectUri"
        }

        // 7. Keycloak 로그아웃 엔드포인트로 리다이렉트 (이 과정에서 Keycloak 세션 만료 처리)
        response.sendRedirect(keycloakLogoutRedirectUrl)
    }
}
