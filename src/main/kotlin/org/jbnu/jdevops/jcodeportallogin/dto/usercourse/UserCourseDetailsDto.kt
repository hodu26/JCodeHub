package org.jbnu.jdevops.jcodeportallogin.dto.usercourse

import org.jbnu.jdevops.jcodeportallogin.dto.assignment.AssignmentDto

data class UserCourseDetailsDto(
    val courseId: Long,
    val courseName: String,
    val courseCode: String,
    val courseProfessor: String,
    val courseYear: Int,
    val courseTerm: Int,
    val courseClss: Int,
    val assignments: List<AssignmentDto>,
    val jcodeUrl: String?
)