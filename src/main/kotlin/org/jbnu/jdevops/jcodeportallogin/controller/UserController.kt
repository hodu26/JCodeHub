package org.jbnu.jdevops.jcodeportallogin.controller

import org.jbnu.jdevops.jcodeportallogin.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest
import org.jbnu.jdevops.jcodeportallogin.dto.*
import org.jbnu.jdevops.jcodeportallogin.service.JwtAuthService

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val jwtAuthService: JwtAuthService
) {

    // 유저 정보 조회
    @GetMapping("/info")
    fun getUserInfo(request: HttpServletRequest): ResponseEntity<UserInfoDto> {
        val email = extractEmailFromRequest(request)
        val userInfo = userService.getUserInfo(email)
        return ResponseEntity.ok(userInfo)
    }

    // 유저별 강의 정보 조회
    @GetMapping("/courses")
    fun getUserCourses(request: HttpServletRequest): ResponseEntity<List<UserCoursesDto>> {
        val email = extractEmailFromRequest(request)
        val courses = userService.getUserCourses(email)
        return ResponseEntity.ok(courses)
    }

    private fun extractEmailFromRequest(request: HttpServletRequest): String {
        val authHeader = request.getHeader("Authorization")
            ?: throw IllegalArgumentException("Authorization header is missing")

        if (!authHeader.startsWith("Bearer ")) {
            throw IllegalArgumentException("Invalid token format")
        }

        val token = authHeader.substring(7)
        return jwtAuthService.extractEmail(token)
    }

    // 유저별 JCode 정보 조회
    @GetMapping("/jcodes")
    fun getUserJcodes(request: HttpServletRequest): ResponseEntity<List<JCodeDto>> {
        val email = extractEmailFromToken(request)
        return ResponseEntity.ok(userService.getUserJcodes(email))
    }

    // 유저별 참가 강의의 과제 및 JCode 정보 조회
    @GetMapping("/courses/details")
    fun getUserCoursesWithDetails(request: HttpServletRequest): ResponseEntity<List<UserCourseDetailsDto>> {
        val email = extractEmailFromToken(request)
        return ResponseEntity.ok(userService.getUserCoursesWithDetails(email))
    }

    // 유저 강의 가입
    @PostMapping("/courses/{courseId}/join")
    fun joinCourse(
        @PathVariable courseId: Long,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        val email = extractEmailFromToken(request)
        userService.joinCourse(email, courseId)
        return ResponseEntity.ok("Successfully joined the course")
    }

    // 유저 강의 탈퇴
    @DeleteMapping("/courses/{courseId}/leave")
    fun leaveCourse(@PathVariable courseId: Long, request: HttpServletRequest): ResponseEntity<String> {
        val email = extractEmailFromRequest(request)
        userService.leaveCourse(courseId, email)
        return ResponseEntity.ok("User successfully left the course")
    }

    // JWT 토큰에서 이메일 파싱
    private fun extractEmailFromToken(request: HttpServletRequest): String {
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
            ?: throw IllegalArgumentException("Missing Authorization Header")

        return jwtAuthService.extractEmail(token)
    }


    @GetMapping
    fun getAllUsers(): List<UserDto> {
        return userService.getAllUsers()
    }

    @GetMapping("/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<UserDto> {
        return userService.getUserByEmail(email)?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{email}")
    fun deleteUser(@PathVariable email: String): ResponseEntity<Unit> {
        return try {
            userService.deleteUser(email)
            ResponseEntity.ok().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

}
