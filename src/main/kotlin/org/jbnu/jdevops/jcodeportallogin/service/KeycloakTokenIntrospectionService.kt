package org.jbnu.jdevops.jcodeportallogin.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit

@Service
class KeycloakTokenIntrospectionService(
    private val redisTemplate: StringRedisTemplate
) {

    @Value("\${keycloak.introspect-url}")
    private lateinit var introspectUrl: String

    @Value("\${keycloak.client-id}")
    private lateinit var clientId: String

    @Value("\${keycloak.client-secret}")
    private lateinit var clientSecret: String

    private val restTemplate = RestTemplate()

    fun introspectToken(token: String): Boolean {
        // Redis 블랙리스트 확인
        if (isTokenBlacklisted(token)) {
            return false
        }

        val requestHeaders = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val requestBody = "token=$token&client_id=$clientId&client_secret=$clientSecret"
        val requestEntity = HttpEntity(requestBody, requestHeaders)

        val response = restTemplate.exchange(
            introspectUrl,
            HttpMethod.POST,
            requestEntity,
            Map::class.java
        )

        val responseBody = response.body ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token")

        val isActive = responseBody["active"] as Boolean
        if (!isActive) {
            addToBlacklist(token)  // Keycloak에서 만료된 토큰을 블랙리스트 추가
        }

        return isActive
    }

    private fun isTokenBlacklisted(token: String): Boolean {
        return redisTemplate.hasKey("blacklist:$token")
    }

    private fun addToBlacklist(token: String) {
        redisTemplate.opsForValue().set("blacklist:$token", "1", 10, TimeUnit.MINUTES)
    }
}
