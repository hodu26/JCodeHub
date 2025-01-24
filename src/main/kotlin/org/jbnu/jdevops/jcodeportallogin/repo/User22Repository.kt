package org.jbnu.jdevops.jcodeportallogin.repo

import org.jbnu.jdevops.jcodeportallogin.entity.User22
import org.springframework.data.jpa.repository.JpaRepository

interface User22Repository : JpaRepository<User22, String> {
    fun findByEmail(email: String): User22?
}
