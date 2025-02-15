package org.jbnu.jdevops.jcodeportallogin.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.dto.LoginUserDto

import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.repo.LoginRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.jbnu.jdevops.jcodeportallogin.service.token.JwtAuthService
import org.jbnu.jdevops.jcodeportallogin.util.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val loginRepository: LoginRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtAuthService: JwtAuthService,
    private val redisService: RedisService,
    private val jwtUtil: JwtUtil
) {

    fun basicLogin(loginUserDto: LoginUserDto): Map<String, String> {
        val user = userRepository.findByEmail(loginUserDto.email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val login = loginRepository.findByUserId(user.id)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")

        if (!passwordEncoder.matches(loginUserDto.password, login.password)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password")
        }

        // JWT 토큰 생성 (이메일 + 학교 정보)
        val jwt = jwtAuthService.createToken(user.email, RoleType.STUDENT)
        return mapOf("message" to "Login successful", "token" to jwt)
    }

    // 검증: refresh token 유효성, 블랙리스트, Redis에 저장된 값 일치 여부 확인
    // 재발급: 새로운 access token과 refresh token 생성 후 Redis 및 쿠키/헤더 갱신 (RTR)
    fun refreshTokens(request: HttpServletRequest, response: HttpServletResponse): Map<String, String> {
        // 1. refresh token 추출 (쿠키 이름 "refreshToken")
        val refreshToken = request.cookies?.firstOrNull { it.name == "refreshToken" }?.value
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "No refresh token provided")

        // 2. refresh token 검증
        if (!jwtAuthService.validateRefreshToken(refreshToken)) {
            response.sendRedirect("/logout")
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalid or expired")
        }
        if (redisService.isJwtBlacklisted(refreshToken)) {
            response.sendRedirect("/logout")
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token blacklisted")
        }

        // 3. refresh token의 클레임에서 사용자 정보 추출
        val refreshClaims = jwtAuthService.getClaims(refreshToken)
        val email = refreshClaims.subject

        // 4. Redis에 저장된 refresh token과 비교
        val storedRefreshToken = redisService.getRefreshToken(email)
        if (storedRefreshToken == null || storedRefreshToken != refreshToken) {
            response.sendRedirect("/logout")
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Stored refresh token mismatch")
        }

        // 5. 사용자 조회
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
        val role = user.role

        // 6. 새 access token 및 refresh token 생성 (RTR 적용)
        val newAccessToken = jwtAuthService.createToken(email, role)
        val newRefreshToken = jwtAuthService.createRefreshToken(email, role)

        // 7. 업데이트: Redis에 새로운 refresh token 저장 및 쿠키/헤더 갱신
        redisService.storeRefreshToken(email, newRefreshToken)
        // Access Token은 응답 헤더에 "Bearer" 형식으로 전달
        response.setHeader("Authorization", "Bearer $newAccessToken")
        // Refresh Token은 쿠키로 갱신
        response.addCookie(jwtUtil.createJwtCookie("refreshToken", newRefreshToken))

        return mapOf("message" to "Tokens refreshed")
    }
}
