package org.jbnu.jdevops.jcodeportallogin.controller

import org.jbnu.jdevops.jcodeportallogin.dto.JCodeDto
import org.jbnu.jdevops.jcodeportallogin.dto.JCodeRequestDto
import org.jbnu.jdevops.jcodeportallogin.service.JCodeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/jcode")
class JCodeController(
    private val jCodeService: JCodeService
) {

    // JCode 추가
    @PostMapping("/{courseId}")
    fun createJCode(
        @PathVariable courseId: Long,
        @RequestBody request: JCodeRequestDto
    ): ResponseEntity<JCodeDto> {
        return ResponseEntity.ok(jCodeService.createJCode(courseId, request.jcodeUrl, request.email))
    }

    // JCode 삭제
    @DeleteMapping("/{jcodeId}")
    fun deleteJCode(@PathVariable jcodeId: Long): ResponseEntity<String> {
        jCodeService.deleteJCode(jcodeId)
        return ResponseEntity.ok("JCode deleted successfully")
    }
}
