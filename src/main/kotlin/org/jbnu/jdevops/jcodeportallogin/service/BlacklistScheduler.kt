package org.jbnu.jdevops.jcodeportallogin.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class BlacklistScheduler(
    private val redisTemplate: StringRedisTemplate,
    private val keycloakAuthService: KeycloakAuthService
) {
    @Scheduled(fixedRate = 600000)  // 10분마다 실행
    fun updateBlacklist() {
        val tokens = redisTemplate.keys("blacklist:*") ?: return
        tokens.forEach { tokenKey ->
            val token = tokenKey.removePrefix("blacklist:")
            if (!keycloakAuthService.validateToken(token)) {
                redisTemplate.opsForValue().set("blacklist:$token", "1", 10, TimeUnit.MINUTES)
            }
        }
    }
}
