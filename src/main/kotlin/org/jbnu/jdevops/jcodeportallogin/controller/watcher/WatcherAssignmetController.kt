package org.jbnu.jdevops.jcodeportallogin.controller.watcher

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.jbnu.jdevops.jcodeportallogin.dto.watcher.AssingmentTotalGraphListData
import org.jbnu.jdevops.jcodeportallogin.dto.watcher.WatcherAssignmentDto
import org.jbnu.jdevops.jcodeportallogin.service.watcher.WatcherAssignmentService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@Tag(name = "Watcher Assignment API", description = "Watcher 과제 수집 정보 중계 API")
@RestController
@RequestMapping("/api/watcher/assignments")
class WatcherAssignmetController(private val watcherAssignmentService: WatcherAssignmentService) {

    @Operation(
        summary = "과제 데이터 조회",
        description = "지정된 courseId와 assignmentId에 대한 과제 데이터를 조회합니다.",
    )
    @GetMapping("{assignmentId}/courses/{courseId}")
    fun getAssignmentsData(
        @PathVariable courseId: Long,        // courseId
        @PathVariable assignmentId: Long,    // assignmentId
    ): ResponseEntity<WatcherAssignmentDto> {
        val result = watcherAssignmentService.getAssignmentsData(courseId, assignmentId)

        return if (result != null) ResponseEntity.ok(result)
        else ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "지정 기간동안의 과제 데이터 그래프 조회",
        description = "지정 기간동안 courseId와 assignmentId에 대한 과제 데이터를 조회합니다.",
    )
    @GetMapping("{assignmentId}/courses/{courseId}/between")
    fun getAssignmentsData(
        @PathVariable courseId: Long,        // courseId
        @PathVariable assignmentId: Long,    // assignmentId
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        st: LocalDateTime,                   // start-date
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        end: LocalDateTime,                  // end-date
    ): ResponseEntity<AssingmentTotalGraphListData> {
        val result = watcherAssignmentService.getAssignmentsTotalGraphData(courseId, assignmentId, st, end)

        return if (result != null) ResponseEntity.ok(result)
        else ResponseEntity.notFound().build()
    }
}