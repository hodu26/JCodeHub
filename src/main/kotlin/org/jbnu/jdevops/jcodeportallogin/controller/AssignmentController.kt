package org.jbnu.jdevops.jcodeportallogin.controller

import org.jbnu.jdevops.jcodeportallogin.dto.AssignmentDto
import org.jbnu.jdevops.jcodeportallogin.service.AssignmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/assignment")
class AssignmentController(
    private val assignmentService: AssignmentService
) {

    // 과제 추가
    @PostMapping("/{courseId}")
    fun createAssignment(@PathVariable courseId: Long, @RequestBody assignmentDto: AssignmentDto): ResponseEntity<AssignmentDto> {
        return ResponseEntity.ok(assignmentService.createAssignment(courseId, assignmentDto))
    }

    // 과제 수정
    @PutMapping("/{courseId}/{assignmentId}")
    fun updateAssignment(@PathVariable courseId: Long, @PathVariable assignmentId: Long, @RequestBody assignmentDto: AssignmentDto): ResponseEntity<AssignmentDto> {
        return ResponseEntity.ok(assignmentService.updateAssignment(courseId, assignmentId, assignmentDto))
    }

    // 과제 삭제
    @DeleteMapping("/{courseId}/{assignmentId}")
    fun deleteAssignment(@PathVariable courseId: Long, @PathVariable assignmentId: Long): ResponseEntity<String> {
        assignmentService.deleteAssignment(courseId, assignmentId)
        return ResponseEntity.ok("Assignment deleted successfully")
    }
}
