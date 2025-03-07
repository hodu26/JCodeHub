package org.jbnu.jdevops.jcodeportallogin.dto

import org.jbnu.jdevops.jcodeportallogin.entity.RoleType

class UserRoleChangeDto (
    val newRole: RoleType,
    val courseId: Long? = null
)