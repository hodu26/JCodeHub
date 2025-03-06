package org.jbnu.jdevops.jcodeportallogin.controller.watcher

import io.swagger.v3.oas.annotations.tags.Tag
import org.jbnu.jdevops.jcodeportallogin.dto.watcher.GraphDataListDto
import org.jbnu.jdevops.jcodeportallogin.dto.watcher.WatcherAssignmentDto
import org.jbnu.jdevops.jcodeportallogin.service.watcher.WatcherAssignmentService
import org.jbnu.jdevops.jcodeportallogin.service.watcher.WatcherStudentService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Watcher Assignment API", description = "Watcher 정보 수집 중계 API")
@RestController
@RequestMapping("/api/watcher/assignments")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'ASSISTANT')") // ADMIN 권한이 없는 사용자는 모두 접근 불가
class WatcherAssignmetController(private val watcherAssignmentService: WatcherAssignmentService) {
    @GetMapping("{assignmentId}/courses/{courseId}")
    fun getAssignmentsData(
        @PathVariable courseId: Long,        // courseId
        @PathVariable assignmentId: Long,    // assignmentId
    ): ResponseEntity<WatcherAssignmentDto> {
        val result = watcherAssignmentService.getAssignmentsData(courseId, assignmentId)

        return if (result != null) ResponseEntity.ok(result)
        else ResponseEntity.notFound().build()
    }
}