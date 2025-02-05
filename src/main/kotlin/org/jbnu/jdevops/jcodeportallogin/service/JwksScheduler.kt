package org.jbnu.jdevops.jcodeportallogin.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class JwksScheduler(
    private val keycloakJwksService: KeycloakJwksService
) {

    // 10분마다 Keycloak JWKS를 갱신
    @Scheduled(fixedRate = 600000) // 10분 마다 실행 (600000ms)
    fun refreshJwks() {
        try {
            keycloakJwksService.getPublicKey()  // 새로운 JWKS를 Redis에 저장
            println("JWKS updated successfully in Redis")
        } catch (e: Exception) {
            println("JWKS update failed: ${e.message}")
        }
    }
}
