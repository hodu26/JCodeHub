package org.jbnu.jdevops.jcodeportallogin.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long = 0,  // JPA가 자동으로 설정

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = true)
    val name: String? = null,

    @Column(nullable = true)
    val studentNum: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: RoleType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val school: SchoolType,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)