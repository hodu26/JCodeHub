package org.jbnu.jdevops.jcodeportallogin.service.watcher

import org.jbnu.jdevops.jcodeportallogin.dto.watcher.AssingmentTotalGraphListData
import org.jbnu.jdevops.jcodeportallogin.dto.watcher.WatcherAssignmentDto
import org.jbnu.jdevops.jcodeportallogin.repo.AssignmentRepository
import org.jbnu.jdevops.jcodeportallogin.repo.CourseRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class WatcherAssignmentService(
    private val webClient: WebClient,
    private val assignmentRepository: AssignmentRepository,
    private val courseRepository: CourseRepository
) {
    fun getAssignmentsData(courseId: Long, assignmentId: Long): WatcherAssignmentDto? {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        val assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found") }

        val classDiv = "${course.code}-${course.clss}"

        return try {
            webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/api/assignment/{class_div}/{hw_name}")
                        .build(classDiv, assignment.name)
                }
                .retrieve()
                .bodyToMono(WatcherAssignmentDto::class.java)
                .block()
        } catch (ex: Exception) {
            println("Error calling external API: ${ex.message}")
            null
        }
    }

    fun getAssignmentsTotalGraphData(courseId: Long, assignmentId: Long, st: LocalDateTime, end: LocalDateTime): AssingmentTotalGraphListData? {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        val assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found") }

        val classDiv = "${course.code}-${course.clss}"

        return try {
            webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/api/total_graph_data/{class_div}/{hw_name}/{st}/{end}")
                        .build(classDiv, assignment.name, st, end)
                }
                .retrieve()
                .bodyToMono(AssingmentTotalGraphListData::class.java)
                .block()
        } catch (ex: Exception) {
            println("Error calling external API: ${ex.message}")
            null
        }
    }
}