package org.jbnu.jdevops.jcodeportallogin.service.watcher

import org.jbnu.jdevops.jcodeportallogin.dto.watcher.WatcherSelectionsDto
import org.jbnu.jdevops.jcodeportallogin.repo.AssignmentRepository
import org.jbnu.jdevops.jcodeportallogin.repo.CourseRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserCoursesRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException

@Service
class WatcherSelectionService (
    @Qualifier("watcherWebClient")
    private val webClient: WebClient,
    private val assignmentRepository: AssignmentRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val userCoursesRepository: UserCoursesRepository
) {
    fun getFileSelections(courseId: Long, assignmentId: Long, userId: Long): List<String>? {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        val assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found") }

        val user = userRepository.findById(userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val userCourse = userCoursesRepository.findByUserIdAndCourseId(user.id, course.id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "UserCourse not found")

        val classDiv = "${course.code.lowercase()}-${course.clss}"

        return try {
            webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/api/{class_div}/{hw_name}/{student_num}")
                        .build(classDiv, assignment.name, user.studentNum)
                }
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<List<String>>() {})  // List 파싱
                .block()
        } catch (ex: Exception) {
            println("Error calling external API: ${ex.message}")
            null
        }
    }

    fun getTimestampSelections(filename: String, courseId: Long, assignmentId: Long, userId: Long): List<String>? {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        val assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found") }

        val user = userRepository.findById(userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val userCourse = userCoursesRepository.findByUserIdAndCourseId(user.id, course.id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "UserCourse not found")

        val classDiv = "${course.code.lowercase()}-${course.clss}"

        return try {
            webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/api/{class_div}/{hw_name}/{student_num}/{filename}")
                        .build(classDiv, assignment.name, user.studentNum, filename)
                }
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<List<String>>() {})  // List 파싱
                .block()
        } catch (ex: Exception) {
            println("Error calling external API: ${ex.message}")
            null
        }
    }
}