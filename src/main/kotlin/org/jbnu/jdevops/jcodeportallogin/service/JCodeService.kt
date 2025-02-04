package org.jbnu.jdevops.jcodeportallogin.service

import org.jbnu.jdevops.jcodeportallogin.dto.JCodeDto
import org.jbnu.jdevops.jcodeportallogin.entity.Jcode
import org.jbnu.jdevops.jcodeportallogin.repo.JCodeRepository
import org.jbnu.jdevops.jcodeportallogin.repo.CourseRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserCoursesRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class JCodeService(
    private val jCodeRepository: JCodeRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val userCoursesRepository: UserCoursesRepository
) {

    // JCode 생성
    fun createJCode(courseId: Long, jcodeUrl: String, email: String): JCodeDto {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val userCourse = userCoursesRepository.findByUserAndCourse(user, course)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "UserCourse not found")

        val jCode = jCodeRepository.save(
            Jcode(
                jcodeUrl = jcodeUrl,
                course = course,
                user = user,
                userCourse = userCourse
            )
        )

        // UserCourses 테이블의 jcode 값 변경 (JCode 생성 시 true)
        val updatedUserCourse = userCourse.copy(jcode = true)
        userCoursesRepository.save(updatedUserCourse)

        return JCodeDto(jcodeId = jCode.jcodeId, jcodeUrl = jCode.jcodeUrl, courseName = jCode.course.name)
    }

    // JCode 삭제 (JCode ID를 받아 삭제)
    fun deleteJCode(jcodeId: Long) {
        val jCode = jCodeRepository.findById(jcodeId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "JCode not found") }

        val userCourse = jCode.userCourse

        jCodeRepository.delete(jCode)

        // UserCourses 테이블의 jcode 값 변경 (JCode 삭제 시 false)
        val updatedUserCourse = userCourse.copy(jcode = false)
        userCoursesRepository.save(updatedUserCourse)
    }
}
