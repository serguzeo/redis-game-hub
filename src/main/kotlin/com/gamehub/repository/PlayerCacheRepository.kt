package com.gamehub.repository

import com.gamehub.config.RedisKeys
import com.gamehub.model.Player
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

/** Кеш профилей: String `cache:player:{id}` с JSON и TTL 60 сек. */
@Repository
class PlayerCacheRepository(
    private val redis: StringRedisTemplate,
    private val jsonMapper: JsonMapper,
) {

    /** GET cache:player:{id} */
    fun get(id: String): Player? =
        redis.opsForValue().get(RedisKeys.playerCache(id))?.let { jsonMapper.readValue<Player>(it) }

    /** SET cache:player:{id} {json} EX 60 */
    fun put(player: Player) {
        redis.opsForValue().set(RedisKeys.playerCache(player.id), jsonMapper.writeValueAsString(player), RedisKeys.PROFILE_CACHE_TTL)
    }

    /** DEL cache:player:{id} [cache:player:{id2} ...] */
    fun evict(vararg ids: String) {
        redis.delete(ids.map(RedisKeys::playerCache))
    }
}
