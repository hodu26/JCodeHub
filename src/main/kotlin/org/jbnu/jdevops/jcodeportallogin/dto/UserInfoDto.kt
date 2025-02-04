package org.jbnu.jdevops.jcodeportallogin.dto

import org.jbnu.jdevops.jcodeportallogin.entity.RoleType

// 유저 정보 DTO
data class UserInfoDto(
    val email: String,
    val role: RoleType,
    val studentNum: Int?
)