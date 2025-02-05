package org.jbnu.jdevops.jcodeportallogin.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.dto.LoginUserDto
import org.jbnu.jdevops.jcodeportallogin.dto.RegisterUserDto
import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.entity.User
import org.jbnu.jdevops.jcodeportallogin.service.*
import org.jbnu.jdevops.jcodeportallogin.util.JwtUtil
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val keycloakAuthService: KeycloakAuthService,
) {

    // 일반 로그인 ( ADMIN, PROFESSOR, ASSISTANT )
    @PostMapping("/login/basic")
    fun basicLogin(@RequestBody loginUserDto: LoginUserDto, response: HttpServletResponse): ResponseEntity<Map<String, String>> {
        val result = authService.basicLogin(loginUserDto)
        response.addCookie(JwtUtil.createJwtCookie("jwt_auth", result["token"] ?: ""))
        return ResponseEntity.ok(result)
    }

    // KeyCloak 로그인 ( STUDENT )
    @GetMapping("/login/oidc/success")
    fun loginOidcSuccess(
        authentication: Authentication,
        response: HttpServletResponse
    ): ResponseEntity<Map<String, String>> {

        val email = authentication.principal as? String
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing email in authentication")

        val roles = authentication.authorities.map { it.authority }
        println("✅ Extracted roles: $roles")

        // 🔹 기존 서비스 로직 유지
        val result = authService.oidcLogin(email, roles)
        return ResponseEntity.ok(result)
    }


    @PostMapping("/signup")
    fun register(@RequestBody registerUserDto: RegisterUserDto): ResponseEntity<String> {
        return userService.register(registerUserDto)
    }

    // Node.js 서버로 리다이렉션 (JCode)
    @GetMapping("/redirect-to-node")
    fun redirectToNode(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestParam courseCode: String
    ): ResponseEntity<Map<String, String>> {

        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization Header")

        // **Keycloak Access Token 검증**
        if (!keycloakAuthService.validateToken(token)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or Expired Access Token")
        }

        // Node.js 서버 URL 설정
        val nodeJsUrl = "http://localhost:3001/jcode?courseCode=$courseCode"

        // **Keycloak Access Token을 HTTP-Only Secure 쿠키로 설정**
        response.addCookie(JwtUtil.createJwtCookie("jwt", token))

        return ResponseEntity.ok(mapOf("redirectUrl" to nodeJsUrl))
    }

    // 학생 계정 추가
    @PostMapping("/student")
    fun registerStudent(@RequestBody registerUserDto: RegisterUserDto): ResponseEntity<String> {
        val studentDto = registerUserDto.copy(role = RoleType.STUDENT)
        return userService.register(studentDto)
    }

    // 조교 계정 추가
    @PostMapping("/assistant")
    fun registerAssistant(@RequestBody registerUserDto: RegisterUserDto): ResponseEntity<String> {
        val assistantDto = registerUserDto.copy(role = RoleType.ASSISTANCE)
        return userService.register(assistantDto)
    }

    // 교수 계정 추가
    @PostMapping("/professor")
    fun registerProfessor(@RequestBody registerUserDto: RegisterUserDto): ResponseEntity<String> {
        val professorDto = registerUserDto.copy(role = RoleType.PROFESSOR)
        return userService.register(professorDto)
    }

    // 현재 로그인된 사용자 정보 반환
    @GetMapping("/me")
    fun getCurrentUser(): Map<String, String> {
        val authentication = SecurityContextHolder.getContext().authentication
        val token = authentication.name

        return mapOf("token" to token, "message" to "User is authenticated")
    }
}