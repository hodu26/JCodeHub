package org.jbnu.jdevops.jcodeportallogin.controller.watcher

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.jbnu.jdevops.jcodeportallogin.dto.watcher.GraphDataListDto
import org.jbnu.jdevops.jcodeportallogin.dto.watcher.SnapshotAvgDto
import org.jbnu.jdevops.jcodeportallogin.service.watcher.WatcherStudentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@Tag(name = "Watcher API", description = "Watcher 스냅샷 수집 정보 중계 API")
@RestController
@RequestMapping("/api/watcher")
class WatcherController(private val watcherStudentService: WatcherStudentService) {

    @Operation(
        summary = "파일 스냅샷 평균 데이터 조회",
        description = "특정 파일의 스냅샷 평균 데이터를 조회합니다."
    )
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

    @Operation(
        summary = "과제 스냅샷 평균 데이터 조회",
        description = "특정 과제의 스냅샷 평균 데이터를 조회합니다."
    )
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

    @Operation(
        summary = "그래프 데이터 조회",
        description = "지정된 interval, course, assignment, user에 대한 그래프 데이터를 조회합니다."
    )
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