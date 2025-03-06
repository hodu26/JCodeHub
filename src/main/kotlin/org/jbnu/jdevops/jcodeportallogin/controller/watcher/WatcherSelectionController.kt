package org.jbnu.jdevops.jcodeportallogin.controller.watcher

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.jbnu.jdevops.jcodeportallogin.dto.watcher.WatcherSelectionsDto
import org.jbnu.jdevops.jcodeportallogin.service.watcher.WatcherSelectionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Watcher Selection API", description = "Watcher 선택 목록 중계 API")
@RestController
@RequestMapping("/api/watcher/selections")
class WatcherSelectionController(private val watcherSelectionService: WatcherSelectionService) {

    @Operation(
        summary = "파일 선택 목록 조회",
        description = "특정 course, assignment, user에 대한 파일 선택 데이터를 조회합니다."
    )
    @GetMapping
    fun getFileSelections(
        @RequestParam course: Long,        // courseId
        @RequestParam assignment: Long,    // assignmentId
        @RequestParam user: Long           // userId
    ): ResponseEntity<WatcherSelectionsDto> {
        val selections = watcherSelectionService.getFileSelections(course, assignment, user)

        return if (selections != null) ResponseEntity.ok(WatcherSelectionsDto(selections))
        else ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "타임스탬프 선택 목록 조회",
        description = "특정 filename과 course, assignment, user에 대한 타임스탬프 선택 데이터를 조회합니다."
    )
    @GetMapping("/files/{filename}")
    fun getTimestampSelections(
        @PathVariable filename: String,
        @RequestParam course: Long,        // courseId
        @RequestParam assignment: Long,    // assignmentId
        @RequestParam user: Long           // userId
    ): ResponseEntity<WatcherSelectionsDto> {
        val selections = watcherSelectionService.getTimestampSelections(filename, course, assignment, user)

        return if (selections != null) ResponseEntity.ok(WatcherSelectionsDto(selections))
        else ResponseEntity.notFound().build()
    }
}