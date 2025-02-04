package org.jbnu.jdevops.jcodeportallogin.dto

data class UserCourseDetailsDto(
    val courseName: String,
    val courseCode: String,
    val assignments: List<AssignmentDto>,
    val jcodeUrl: String?
)