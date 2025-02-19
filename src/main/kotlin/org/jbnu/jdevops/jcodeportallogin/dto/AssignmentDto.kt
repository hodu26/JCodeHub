package org.jbnu.jdevops.jcodeportallogin.dto

import java.time.LocalDateTime

data class AssignmentDto(
    val assignmentId: Long?,
    val assignmentName: String,
    val assignmentDescription: String? = null,
    val kickoffDate: LocalDateTime,
    val deadlineDate: LocalDateTime,
    val createdAt: String?,
    val updatedAt: String?
)