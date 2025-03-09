package org.jbnu.jdevops.jcodeportallogin.dto

data class JCodeRequestDto(
    val namespace: String,
    val deployment_name: String,
    val service_name: String,
    val app_label: String,
    val file_path: String
)

data class JCodeDeleteRequestDto(
    val namespace: String,
    val deployment_name: String,
    val service_name: String
)