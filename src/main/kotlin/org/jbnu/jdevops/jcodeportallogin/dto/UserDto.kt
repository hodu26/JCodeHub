package org.jbnu.jdevops.jcodeportallogin.dto

import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.entity.SchoolType

data class UserDto(
    val email: String,
    val name: String?,
    val role: RoleType = RoleType.STUDENT,
    val school: SchoolType,
    val student_num: Int?  // nullable
)
