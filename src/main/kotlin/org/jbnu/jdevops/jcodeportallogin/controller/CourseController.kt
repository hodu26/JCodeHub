package org.jbnu.jdevops.jcodeportallogin.controller

import org.jbnu.jdevops.jcodeportallogin.dto.AssignmentDto
import org.jbnu.jdevops.jcodeportallogin.dto.CourseDto
import org.jbnu.jdevops.jcodeportallogin.dto.UserInfoDto
import org.jbnu.jdevops.jcodeportallogin.service.CourseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/course")
class CourseController(
    private val courseService: CourseService
) {

    // 강의별 유저 조회
    @GetMapping("/{courseId}/users")
    fun getUsersByCourse(@PathVariable courseId: Long): ResponseEntity<List<UserInfoDto>> {
        return ResponseEntity.ok(courseService.getUsersByCourse(courseId))
    }

    // 강의별 과제 조회
    @GetMapping("/{courseId}/assignments")
    fun getAssignmentsByCourse(@PathVariable courseId: Long): ResponseEntity<List<AssignmentDto>> {
        return ResponseEntity.ok(courseService.getAssignmentsByCourse(courseId))
    }

    // 강의 추가
    @PostMapping
    fun createCourse(@RequestBody courseDto: CourseDto): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(courseService.createCourse(courseDto))
    }

    // 강의 수정
    @PutMapping("/{courseId}")
    fun updateCourse(@PathVariable courseId: Long, @RequestBody courseDto: CourseDto): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(courseService.updateCourse(courseId, courseDto))
    }

    // 강의 삭제
    @DeleteMapping("/{courseId}")
    fun deleteCourse(@PathVariable courseId: Long): ResponseEntity<String> {
        courseService.deleteCourse(courseId)
        return ResponseEntity.ok("Course deleted successfully")
    }
}
