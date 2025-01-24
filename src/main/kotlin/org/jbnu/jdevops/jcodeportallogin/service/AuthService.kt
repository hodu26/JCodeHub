package org.jbnu.jdevops.jcodeportallogin.service

import org.jbnu.jdevops.jcodeportallogin.dto.LoginUserDto
import org.jbnu.jdevops.jcodeportallogin.dto.RegisterUserDto
import org.jbnu.jdevops.jcodeportallogin.entity.Login
import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.entity.SchoolType
import org.jbnu.jdevops.jcodeportallogin.entity.User
import org.jbnu.jdevops.jcodeportallogin.repo.LoginRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val loginRepository: LoginRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    fun basicLogin(loginUserDto: LoginUserDto): Map<String, String> {
        // 사용자 정보 가져오기
        val user = userRepository.findByEmail(loginUserDto.email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        // 로그인 정보 가져오기 (비밀번호 확인)
        val login = loginRepository.findByUser_UserId(user.userId)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")

        // 비밀번호 검증
        if (!passwordEncoder.matches(loginUserDto.password, login.password)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password")
        }

        // JWT 토큰 생성 (이메일 + 학교 정보)
        val jwt = jwtService.createToken(user.email, user.school.toString())
        return mapOf("message" to "Login successful", "token" to jwt)
    }

    fun oidcLogin(email: String): Map<String, String> {
        try {
            // 데이터베이스에서 사용자 확인 후 저장
            val user = userRepository.findByEmail(email) ?: userRepository.save(
                User(
                    email = email,
                    role = RoleType.STUDENT,
                    school = SchoolType.fromEmail(email),
                )
            )

            // JWT 토큰 생성 (이메일 + 학교 정보)
            val jwt = jwtService.createToken(email, user.school.toString())
            return mapOf("message" to "Login successful", "token" to jwt)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "OIDC Login failed")
        }
    }

    fun register(registerUserDto: RegisterUserDto): ResponseEntity<String> {
        // 이메일 중복 확인
        if (userRepository.findByEmail(registerUserDto.email) != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use")
        }

        // 비밀번호 유효성 검사
        if (registerUserDto.password.length < 8) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long")
        }

        return try {
            // 비밀번호 해싱
            val hashedPassword = passwordEncoder.encode(registerUserDto.password)

            // 새 사용자 저장
            val user = userRepository.save(
                User(
                    email = registerUserDto.email,
                    name = registerUserDto.name,
                    role = registerUserDto.role,  // 기본적으로 학생 역할 부여
                    school = SchoolType.fromEmail(registerUserDto.email),
                    studentNum = registerUserDto.student_num
                )
            )

            // 로그인 정보 저장
            loginRepository.save(Login(user = user, password = hashedPassword))

            ResponseEntity.ok("Signup successful")
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register user")
        }
    }
}
