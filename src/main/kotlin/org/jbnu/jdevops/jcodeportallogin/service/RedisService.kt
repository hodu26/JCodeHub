package org.jbnu.jdevops.jcodeportallogin.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisService(
    private val redisTemplate: StringRedisTemplate
) {
    // JCode URL을 Redis에서 가져오기
    fun getJcodeUrl(courseCode: String): String? {
        return redisTemplate.opsForValue().get("course:$courseCode:jcode-url")
    }

    // 이메일 & 강의코드 → JCode URL 저장
    fun storeUserCourse(email: String, courseCode: String, jcodeUrl: String) {
        val key = "user:$email:course:$courseCode"
        redisTemplate.opsForValue().set(key, jcodeUrl, 1, TimeUnit.DAYS) // 유효기간 1일
    }

    // **이메일 & 강의코드 → 저장된 JCode URL 삭제**
    fun deleteUserCourse(email: String, courseCode: String) {
        val key = "user:$email:course:$courseCode"
        redisTemplate.delete(key)
    }

    // 강의코드별 참여자 목록 추가
    fun addUserToCourseList(courseCode: String, email: String) {
        val key = "course:$courseCode:participants"
        redisTemplate.opsForSet().add(key, email)
    }

    // **강의코드별 참여자 목록에서 특정 유저 제거**
    fun removeUserFromCourseList(courseCode: String, email: String) {
        val key = "course:$courseCode:participants"
        redisTemplate.opsForSet().remove(key, email)
    }
}
