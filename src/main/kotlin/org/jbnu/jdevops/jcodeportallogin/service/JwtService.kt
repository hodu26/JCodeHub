package org.jbnu.jdevops.jcodeportallogin.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expire}") private val expireTime: Long
) {

    fun createToken(email: String, url: String): String {
        val claims = Jwts.claims().setSubject(email)
        claims["url"] = url

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expireTime))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }
}
