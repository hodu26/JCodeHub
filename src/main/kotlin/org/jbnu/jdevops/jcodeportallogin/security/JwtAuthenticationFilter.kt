package org.jbnu.jdevops.jcodeportallogin.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.service.token.JwtAuthService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtAuthService: JwtAuthService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 쿠키에서 JWT 추출 (쿠키 이름 "jwt")
        val jwtToken = request.cookies?.firstOrNull { it.name == "jwt" }?.value
        if (!jwtToken.isNullOrEmpty() && jwtAuthService.validateToken(jwtToken)) {
            // JWT에서 전체 클레임 추출
            val claims = jwtAuthService.getClaims(jwtToken)
            val email = claims.subject
            val role = claims["role"] as? String ?: "STUDENT"

            // 권한 설정: 예를 들어, role 값이 STUDENT이면 ROLE_STUDENT로 사용
            val authorities = listOf(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
            // 자격 증명은 인증 후 필요 없으므로 null
            val authentication = UsernamePasswordAuthenticationToken(email, null, authorities)
            SecurityContextHolder.getContext().authentication = authentication
        }
        filterChain.doFilter(request, response)
    }
}
