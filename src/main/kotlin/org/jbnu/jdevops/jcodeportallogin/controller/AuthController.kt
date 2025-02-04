package org.jbnu.jdevops.jcodeportallogin.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.dto.LoginUserDto
import org.jbnu.jdevops.jcodeportallogin.dto.RegisterUserDto
import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.service.AuthService
import org.jbnu.jdevops.jcodeportallogin.service.JwtAuthService
import org.jbnu.jdevops.jcodeportallogin.service.UserService
import org.jbnu.jdevops.jcodeportallogin.util.JwtUtil
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val jwtAuthService: JwtAuthService
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
    fun loginOidcSuccess(authentication: OAuth2AuthenticationToken, response: HttpServletResponse): ResponseEntity<Map<String, String>> {
        val oidcUser = authentication.principal as OidcUser
        val result = authService.oidcLogin(oidcUser.email)
        response.addCookie(JwtUtil.createJwtCookie("jwt_auth", result["token"] ?: ""))

        // 프론트엔드로 리디렉션
        response.sendRedirect("http://localhost:3000/jcode")

        return ResponseEntity.ok(result)
    }

    @PostMapping("/signup")
    fun register(@RequestBody registerUserDto: RegisterUserDto): ResponseEntity<String> {
        return userService.register(registerUserDto)
    }

    // Node.js 서버로 리다이렉션 (JCode)
    @GetMapping("/redirect-to-node")
    fun redirectToNode(request: HttpServletRequest, response: HttpServletResponse): String {
        val jwtCookie = request.cookies?.find { it.name == "jwt" }
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT Token not found in cookies")
        return "redirect:https://jcode.jbnu.ac.kr/jcode/?folder=/config/workspace"
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
    fun getUserInfo(request: HttpServletRequest): ResponseEntity<Map<String, Any>> {
        val jwtCookie = request.cookies?.find { it.name == "jwt_auth" }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Unauthorized"))

        val jwtToken = jwtCookie.value

        // JWT 검증
        if (!jwtAuthService.validateToken(jwtToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Invalid or expired token"))
        }

        val claims = jwtAuthService.getClaims(jwtToken)
        val email = claims.subject
        val role = claims["role"] ?: "UNKNOWN"

        val userInfo = mapOf(
            "email" to email,
            "role" to role
        )

        return ResponseEntity.ok(userInfo)
    }
}