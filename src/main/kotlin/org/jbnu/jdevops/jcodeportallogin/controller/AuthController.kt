package org.jbnu.jdevops.jcodeportallogin.controller

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.dto.LoginUserDto
import org.jbnu.jdevops.jcodeportallogin.dto.RegisterUserDto
import org.jbnu.jdevops.jcodeportallogin.service.AuthService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    // 일반 로그인 ( ADMIN, PROFESSOR, ASSISTANT )
    @PostMapping("/login/basic")
    fun basicLogin(@RequestBody loginUserDto: LoginUserDto, response: HttpServletResponse): ResponseEntity<Map<String, String>> {
        val result = authService.basicLogin(loginUserDto)
        response.addCookie(createJwtCookie(result["token"] ?: ""))
        return ResponseEntity.ok(result)
    }

    // KeyCloak 로그인 ( STUDENT )
    @GetMapping("/login/oidc/success")
    fun loginOidcSuccess(authentication: OAuth2AuthenticationToken, response: HttpServletResponse): ResponseEntity<Map<String, String>> {
        val oidcUser = authentication.principal as OidcUser
        val result = authService.oidcLogin(oidcUser.email)
        println(result["token"] ?: "")
        response.addCookie(createJwtCookie(result["token"] ?: ""))
        return ResponseEntity.ok(result)
    }

    @PostMapping("/signup")
    fun register(@RequestBody registerUserDto: RegisterUserDto): ResponseEntity<String> {
        return authService.register(registerUserDto)
    }

    // Node.js 서버로 리다이렉션 (JCode)
    @GetMapping("/redirect-to-node")
    fun redirectToNode(request: HttpServletRequest, response: HttpServletResponse): String {
        val jwtCookie = request.cookies?.find { it.name == "jwt" }
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT Token not found in cookies")
        return "redirect:https://jcode.jbnu.ac.kr/jcode/?folder=/config/workspace"
    }

    // JWT 쿠키 생성 메서드
    private fun createJwtCookie(jwt: String): Cookie {
        return Cookie("jwt", jwt).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 3600
        }
    }
}