package org.jbnu.jdevops.jcodeportallogin.service

import org.jbnu.jdevops.jcodeportallogin.dto.AssignmentDto
import org.jbnu.jdevops.jcodeportallogin.dto.CourseDto
import org.jbnu.jdevops.jcodeportallogin.dto.UserInfoDto
import org.jbnu.jdevops.jcodeportallogin.entity.Course
import org.jbnu.jdevops.jcodeportallogin.repo.AssignmentRepository
import org.jbnu.jdevops.jcodeportallogin.repo.CourseRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserCoursesRepository
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class CourseService(
    private val userCoursesRepository: UserCoursesRepository,
    private val assignmentRepository: AssignmentRepository,
    private val courseRepository: CourseRepository
) {

    // 강의별 유저 조회
    fun getUsersByCourse(courseId: Long): List<UserInfoDto> {
        val userCourses = userCoursesRepository.findByCourse_CourseId(courseId)

        if (userCourses.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No users found for this course")
        }

        return userCourses.map {
            val user = it.user
            UserInfoDto(
                email = user.email,
                role = user.role,
                studentNum = user.studentNum
            )
        }
    }

    // 강의별 과제 조회
    fun getAssignmentsByCourse(courseId: Long): List<AssignmentDto> {
        val assignments = assignmentRepository.findByCourse_CourseId(courseId)

        if (assignments.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No assignments found for this course")
        }

        return assignments.map {
            AssignmentDto(
                assignmentId = it.assignmentId,
                assignmentName = it.name,
                assignmentDescription = it.description,
                createdAt = it.createdAt.toString(),
                updatedAt = it.updatedAt.toString()
            )
        }
    }

    // 강의 추가
    fun createCourse(courseDto: CourseDto): CourseDto {
        val course = courseRepository.save(Course(name = courseDto.name, code = courseDto.code))
        return CourseDto(courseId = course.courseId, name = course.name, code = course.code)
    }

    // 강의 수정
    fun updateCourse(courseId: Long, courseDto: CourseDto): CourseDto {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        val updatedCourse = course.copy(name = courseDto.name, code = courseDto.code)
        courseRepository.save(updatedCourse)
        return CourseDto(courseId = updatedCourse.courseId, name = updatedCourse.name, code = updatedCourse.code)
    }

    // 강의 삭제
    fun deleteCourse(courseId: Long) {
        if (!courseRepository.existsById(courseId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found")
        }
        courseRepository.deleteById(courseId)
    }
}
