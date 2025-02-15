package org.jbnu.jdevops.jcodeportallogin.security

import io.jsonwebtoken.Claims
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.service.RedisService
import org.jbnu.jdevops.jcodeportallogin.service.token.JwtAuthService
import org.jbnu.jdevops.jcodeportallogin.util.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CustomLogoutSuccessHandler(
    private val redisService: RedisService,
    @Value("\${keycloak.logout.url}") private val keycloakLogoutUrl: String, // Keycloak 로그아웃 엔드포인트
    @Value("\${front.domain}") private val frontDomain: String, // 프론트엔드 도메인
    private val jwtUtil: JwtUtil,
    private val jwtAuthService: JwtAuthService
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
        val authHeader = request.getHeader("Authorization")
        if (!authHeader.isNullOrEmpty() && authHeader.startsWith("Bearer ")) {
            val accessToken = authHeader.substringAfter("Bearer ").trim()
            // JWT 클레임에서 만료시간 가져오기
            val jwtClaims: Claims = jwtAuthService.getClaims(accessToken)
            val ttlMillis = jwtClaims.expiration.time - System.currentTimeMillis()
            // JWT 블랙리스트에 추가 (남은 TTL로 설정)
            redisService.addToJwtBlacklist(accessToken, ttlMillis)
        }

        // 3. refresh token 쿠키 추출 (쿠키 이름 "refreshToken")
        val refreshCookie = request.cookies?.firstOrNull { it.name == "refreshToken" }
        if (refreshCookie != null) {
            val refresh = refreshCookie.value
            // refresh token 클레임에서 만료시간 가져오기
            val refreshClaims: Claims = jwtAuthService.getClaims(refresh)
            val refreshTtlMillis = refreshClaims.expiration.time - System.currentTimeMillis()
            // refresh token도 블랙리스트에 추가 (남은 TTL로 설정)
            redisService.addToJwtBlacklist(refresh, refreshTtlMillis)

            // 기존 Redis에 저장된 refresh token 삭제 (이메일을 통해 관리하고 있다면)
            val email = authentication?.principal?.toString()
            if (email != null) {
                redisService.deleteRefreshToken(email)
            }
            // refresh token 쿠키 만료
            response.addCookie(jwtUtil.createExpiredCookie(refreshCookie.name))
        }

        // 4. Redis에서 id_token 조회 및 삭제
        val email = authentication?.principal?.toString()
        var idToken: String? = null
        if (email != null) {
            idToken = redisService.getIdToken(email)
            if (!idToken.isNullOrEmpty()) {
                redisService.deleteIdToken(email)
            }
        }

        // 5. Keycloak 로그아웃 URL 구성: id_token_hint 포함 (있다면)
        val redirectUri = "$frontDomain/"
        val keycloakLogoutRedirectUrl = if (!idToken.isNullOrEmpty()) {
            "$keycloakLogoutUrl?id_token_hint=$idToken&post_logout_redirect_uri=$redirectUri"
        } else {
            "$keycloakLogoutUrl?post_logout_redirect_uri=$redirectUri"
        }

        // 6. Keycloak 로그아웃 엔드포인트로 리다이렉트
        response.sendRedirect(keycloakLogoutRedirectUrl)
    }
}
