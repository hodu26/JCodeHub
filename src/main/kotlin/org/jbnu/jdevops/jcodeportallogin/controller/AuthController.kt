package org.jbnu.jdevops.jcodeportallogin.controller

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.jbnu.jdevops.jcodeportallogin.service.JwtService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Controller
class AuthController(
    private val userRepository: UserRepository,
    private val jwtService: JwtService
) {

    @GetMapping("/login/success")
    fun loginSuccess(authentication: OAuth2AuthenticationToken, response: HttpServletResponse): String {
        val oidcUser = authentication.principal as OidcUser
        val email = oidcUser.email

        // 데이터베이스에서 사용자 정보를 가져옴
        val user = userRepository.findByEmail(email) ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "User not found")

        // JWT 토큰 생성
        val jwt = jwtService.createToken(email, user.url)

        // JWT 토큰을 쿠키로 설정
        val cookie = Cookie("jwt", jwt)
        cookie.isHttpOnly = true
        cookie.secure = true
        cookie.path = "/"
        cookie.maxAge = 3600  // 쿠키의 유효 기간 설정 (1시간)

        response.addCookie(cookie)

        // Node.js 서버로 리다이렉션
        return "redirect:https://jcode.jbnu.ac.kr/jcode/?folder=/config/workspace"
    }
}

