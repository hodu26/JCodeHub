package org.jbnu.jdevops.jcodeportallogin.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.UUID

@Service
class RedisService(
    private val redisTemplate: StringRedisTemplate
) {
    // 저장된 사용자 정보 조회
    fun getUserProfile(id: String): MutableMap<String, String> {
        val key = "user:profile:$id"
        return redisTemplate.opsForHash<String, String>().entries(key)
    }

    // JCode URL을 Redis에서 가져오기
    fun getJcodeUrl(courseCode: String): String? {
        return redisTemplate.opsForValue().get("course:$courseCode:jcode-url")
    }

    // 이메일 & 강의코드 & 분반 → JCode URL 저장
    fun storeUserCourse(email: String, courseCode: String, courseClss: Int, jcodeUrl: String) {
        val key = "user:$email:course:$courseCode:$courseClss"
        redisTemplate.opsForValue().set(key, jcodeUrl, 180, TimeUnit.DAYS) // 유효기간 6개월 (약 180일)
    }

    // 이메일 & 강의코드 & 분반 → 저장된 JCode URL 삭제
    fun deleteUserCourse(email: String, courseCode: String, courseClss: Int) {
        val key = "user:$email:course:$courseCode:$courseClss"
        redisTemplate.delete(key)
    }

    // 강의 참여자 목록에 유저 추가
    fun addUserToCourseList(courseCode: String, courseClss: Int, email: String) {
        val key = "course:$courseCode:$courseClss:participants"
        redisTemplate.opsForSet().add(key, email)
    }

    // 강의코드별 참여자 목록에서 특정 유저 제거
    fun removeUserFromCourseList(courseCode: String, courseClss: Int, email: String) {
        val key = "course:$courseCode:$courseClss:participants"
        redisTemplate.opsForSet().remove(key, email)
    }

    // id_token을 하나의 Redis 해시로 저장 (key: "user:id_tokens", field: 이메일, value: id_token)
    fun storeIdToken(email: String, idToken: String) {
        val hashKey = "user:id_tokens"
        redisTemplate.opsForHash<String, String>().put(hashKey, email, idToken)
        // 해시 전체에 TTL을 설정하고 싶다면, 해시 key에 TTL을 부여할 수 있습니다.
        // redisTemplate.expire(hashKey, 1, TimeUnit.HOURS)
    }

    fun getIdToken(email: String): String? {
        val hashKey = "user:id_tokens"
        return redisTemplate.opsForHash<String, String>().get(hashKey, email)
    }

    fun deleteIdToken(email: String) {
        val hashKey = "user:id_tokens"
        redisTemplate.opsForHash<String, String>().delete(hashKey, email)
    }

    // refresh token을 하나의 Redis 해시로 관리 (key: "user:refresh_tokens", field: 이메일, value: refresh token)
    fun storeRefreshToken(email: String, refreshToken: String) {
        val hashKey = "user:refresh_tokens"
        redisTemplate.opsForHash<String, String>().put(hashKey, email, refreshToken)
    }

    fun getRefreshToken(email: String): String? {
        val hashKey = "user:refresh_tokens"
        return redisTemplate.opsForHash<String, String>().get(hashKey, email)
    }

    fun deleteRefreshToken(email: String) {
        val hashKey = "user:refresh_tokens"
        redisTemplate.opsForHash<String, String>().delete(hashKey, email)
    }

    // JWT 블랙리스트에 저장 (key: "blacklist:jwt:<token>")
    // 인자로 받은 만료 시간(expireTimeMillis)을 사용하여 TTL을 JWT 만료 시간과 같게 설정
    fun addToJwtBlacklist(jwt: String, expireTimeMillis: Long) {
        val key = "blacklist:jwt:$jwt"
        redisTemplate.opsForValue().set(key, "true", expireTimeMillis, TimeUnit.MILLISECONDS)
    }

    fun isJwtBlacklisted(jwt: String): Boolean {
        val key = "blacklist:jwt:$jwt"
        return redisTemplate.hasKey(key) ?: false
    }

    // 사용자 정보를 하나의 해시로 저장 (키: "user:profile:<UUID>")
    fun storeUserProfile(email: String, studentNumber: String, courseCode: String, clss: String) {
        // UUID 생성
        val id = UUID.randomUUID().toString()
        // 키 예시: user:profile:123e4567-e89b-12d3-a456-426614174000
        val key = "user:profile:$id"
        // 저장할 필드와 값을 Map으로 구성
        val userInfo = mapOf(
            "email" to email,
            "studentNumber" to studentNumber,
            "courseCode" to courseCode,
            "clss" to clss
        )
        // Redis 해시에 여러 필드를 한 번에 저장
        redisTemplate.opsForHash<String, String>().putAll(key, userInfo)

        // 필요시 id를 반환하거나 로깅 등 추가 처리 가능
    }
}
