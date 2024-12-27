package org.jbnu.jdevops.jcodeportallogin.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class User(
    @Id val email: String,
    val url: String
)
