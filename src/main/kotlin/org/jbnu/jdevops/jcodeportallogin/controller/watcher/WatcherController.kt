package org.jbnu.jdevops.jcodeportallogin.controller.watcher

import io.swagger.v3.oas.annotations.tags.Tag
import org.jbnu.jdevops.jcodeportallogin.dto.watcher.GraphDataDto
import org.jbnu.jdevops.jcodeportallogin.dto.watcher.GraphDataListDto
import org.jbnu.jdevops.jcodeportallogin.dto.watcher.SnapshotAvgDto
import org.jbnu.jdevops.jcodeportallogin.service.watcher.WatcherStudentService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@Tag(name = "Watcher API", description = "Watcher 정보 수집 중계 API")
@RestController
@RequestMapping("/api/watcher")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'ASSISTANT')") // ADMIN 권한이 없는 사용자는 모두 접근 불가
class WatcherController(private val watcherStudentService: WatcherStudentService) {

    @GetMapping("/snapshot_avg/files/{fileName}")
    fun getSnapshotAverage(
        @PathVariable fileName: String,
        @RequestParam course: Long,        // courseId
        @RequestParam assignment: Long,    // assignmentId
        @RequestParam user: Long           // userId
    ): ResponseEntity<SnapshotAvgDto> {
        val result = watcherStudentService.getSnapshotAverage(fileName, course, assignment, user)

        return if (result != null) ResponseEntity.ok(result)
        else ResponseEntity.notFound().build()
    }

    @GetMapping("/assignments/snapshot_avg")
    fun getAssingnmentSnapshotAverage(
        @RequestParam course: Long,        // courseId
        @RequestParam assignment: Long,    // assignmentId
        @RequestParam user: Long           // userId
    ): ResponseEntity<SnapshotAvgDto> {
        val result = watcherStudentService.getAssignmentSnapshotAverage(course, assignment, user)

        return if (result != null) ResponseEntity.ok(result)
        else ResponseEntity.notFound().build()
    }

    @GetMapping("/graph_data/interval/{interval}")
    fun getGraphData(
        @PathVariable interval: Long,
        @RequestParam course: Long,        // courseId
        @RequestParam assignment: Long,    // assignmentId
        @RequestParam user: Long           // userId
    ): ResponseEntity<GraphDataListDto> {
        val result = watcherStudentService.getGraphData(interval, course, assignment, user)

        return if (result != null) ResponseEntity.ok(result)
        else ResponseEntity.notFound().build()
    }

}