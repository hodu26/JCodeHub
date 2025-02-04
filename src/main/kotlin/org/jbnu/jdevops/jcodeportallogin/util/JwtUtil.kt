package org.jbnu.jdevops.jcodeportallogin.util

import jakarta.servlet.http.Cookie

object JwtUtil {

    fun createJwtCookie(name: String, jwt: String): Cookie {
        return Cookie(name, jwt).apply {
            isHttpOnly = true  // XSS 공격 방지
            secure = true      // HTTPS에서만 전송
            path = "/"         // 전체 도메인에서 사용 가능
            maxAge = 3600      // 1시간 동안 유효
        }
    }
}