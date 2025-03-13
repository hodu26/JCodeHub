package org.jbnu.jdevops.jcodeportallogin.dto.jcode

data class JCodeDto(
    val jcodeId: Long?,
    val courseName: String
)


data class JCodeMainRequestDto(
    val userEmail: String,
)