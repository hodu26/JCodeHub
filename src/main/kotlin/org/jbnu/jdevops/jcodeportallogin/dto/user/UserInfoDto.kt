package org.jbnu.jdevops.jcodeportallogin.dto.user

import org.jbnu.jdevops.jcodeportallogin.entity.RoleType

// 유저 정보 DTO
data class UserInfoDto(
    val userId: Long? = null,
    val name: String?,
    val email: String,
    val role: RoleType,
    val courseRole: RoleType? = null,
    val studentNum: Int?
)