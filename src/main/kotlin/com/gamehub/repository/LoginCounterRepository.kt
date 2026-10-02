package com.gamehub.repository

import com.gamehub.config.RedisKeys
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Repository

@Repository
class LoginCounterRepository(private val redis: StringRedisTemplate) {

    fun increment(playerId: String): Long = redis.execute(
        SCRIPT,
        listOf(RedisKeys.logins(playerId)),
        RedisKeys.LOGINS_TTL.toSeconds().toString()
    )

    private companion object {
        val SCRIPT: RedisScript<Long> = RedisScript.of(
            """
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))
            end
            return count
            """.trimIndent(),
            Long::class.java,
        )
    }
}
