package org.jbnu.jdevops.jcodeportallogin.entity

enum class SchoolType(val domainPrefix: String) {
    JBNU("jbnu"),
    SKKU("skku");

    companion object {
        fun fromEmail(email: String): SchoolType {
            val domainPrefix = email.substringAfter("@").substringBefore(".").lowercase()
            return entries.find { it.domainPrefix == domainPrefix }
                ?: throw IllegalArgumentException("Unknown school domain: $domainPrefix")
        }
    }
}
