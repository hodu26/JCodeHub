package org.jbnu.jdevops.jcodeportallogin.dto

import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.entity.SchoolType

data class RegisterUserDto(
    val email: String,
    val name: String,
    val student_num: Int,
    val role: RoleType = RoleType.STUDENT, // 기본적으로 학생 역할 부여
    val password: String
) {
    val school: SchoolType
        get() = SchoolType.fromEmail(email)
}