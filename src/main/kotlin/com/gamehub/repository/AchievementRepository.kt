package com.gamehub.repository

import com.gamehub.config.RedisKeys
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository

/** Достижения игрока: Set `achievements:{id}`. */
@Repository
class AchievementRepository(private val redis: StringRedisTemplate) {

    /** SADD achievements:{id} {name} */
    fun add(playerId: String, name: String) {
        redis.opsForSet().add(RedisKeys.achievements(playerId), name)
    }

    /** SISMEMBER achievements:{id} {name} */
    fun contains(playerId: String, name: String): Boolean =
        redis.opsForSet().isMember(RedisKeys.achievements(playerId), name) == true

    /** SINTER achievements:{id1} achievements:{id2} */
    fun common(playerId: String, otherPlayerId: String): Set<String> =
        redis.opsForSet().intersect(RedisKeys.achievements(playerId), RedisKeys.achievements(otherPlayerId)).orEmpty()
}
