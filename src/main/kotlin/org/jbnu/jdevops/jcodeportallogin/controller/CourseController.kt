package org.jbnu.jdevops.jcodeportallogin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.jbnu.jdevops.jcodeportallogin.dto.AssignmentDto
import org.jbnu.jdevops.jcodeportallogin.dto.CourseDto
import org.jbnu.jdevops.jcodeportallogin.dto.UserInfoDto
import org.jbnu.jdevops.jcodeportallogin.service.CourseService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Course API", description = "강의 관련 API (ADMIN, PROFESSOR 전용)")
@RestController
@RequestMapping("/api/courses")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")  // ADMIN, PROFESSOR 권한이 없는 사용자는 모두 접근 불가
class CourseController(
    private val courseService: CourseService
) {
    // 강의별 유저 조회
    @Operation(
        summary = "강의별 유저 조회",
        description = "특정 강의에 등록된 모든 사용자 정보를 조회합니다."
    )
    @GetMapping("/{courseId}/users")
    fun getUsersByCourse(@PathVariable courseId: Long): ResponseEntity<List<UserInfoDto>> {
        return ResponseEntity.ok(courseService.getUsersByCourse(courseId))
    }

    // 강의별 과제 조회
    @Operation(
        summary = "강의별 과제 조회",
        description = "특정 강의에 등록된 모든 과제 정보를 조회합니다."
    )
    @GetMapping("/{courseId}/assignments")
    fun getAssignmentsByCourse(@PathVariable courseId: Long): ResponseEntity<List<AssignmentDto>> {
        return ResponseEntity.ok(courseService.getAssignmentsByCourse(courseId))
    }

    // 강의 추가
    @Operation(
        summary = "강의 추가",
        description = "새로운 강의를 생성합니다."
    )
    @PostMapping
    fun createCourse(@RequestBody courseDto: CourseDto): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(courseService.createCourse(courseDto))
    }

    // 강의 수정
    @Operation(
        summary = "강의 수정",
        description = "특정 강의의 정보를 수정합니다."
    )
    @PutMapping("/{courseId}")
    fun updateCourse(@PathVariable courseId: Long, @RequestBody courseDto: CourseDto): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(courseService.updateCourse(courseId, courseDto))
    }

    // 강의 삭제
    @Operation(
        summary = "강의 삭제",
        description = "특정 강의를 삭제합니다."
    )
    @DeleteMapping("/{courseId}")
    fun deleteCourse(@PathVariable courseId: Long): ResponseEntity<String> {
        courseService.deleteCourse(courseId)
        return ResponseEntity.ok("Course deleted successfully")
    }
}
