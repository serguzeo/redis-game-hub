package com.gamehub.repository

import com.gamehub.config.RedisKeys
import com.gamehub.model.Player
import org.springframework.data.redis.connection.DefaultStringRedisConnection
import org.springframework.data.redis.core.RedisCallback
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Instant

/** Профили игроков: Hash `player:{id}` с полями name, level, region, created_at. */
@Repository
class PlayerRepository(private val redis: StringRedisTemplate) {

    /** HSET player:{id} name .. level .. region .. created_at .. */
    fun save(player: Player) {
        redis.opsForHash<String, String>().putAll(RedisKeys.player(player.id), player.toHash())
    }

    /**
     * Массовое сохранение через pipeline: все HSET уходят в Redis одной пачкой,
     * без ожидания ответа на каждую команду.
     */
    fun saveAll(players: List<Player>) {
        redis.executePipelined(RedisCallback<Any?> { connection ->
            val commands = DefaultStringRedisConnection(connection)
            players.forEach { commands.hMSet(RedisKeys.player(it.id), it.toHash()) }
            null // внутри пайплайна результатов ещё нет, они вернутся списком из executePipelined
        })
    }

    /** HGETALL player:{id}; null, если профиля нет. */
    fun findById(id: String): Player? {
        val hash = redis.opsForHash<String, String>().entries(RedisKeys.player(id))
        if (hash.isEmpty()) return null
        return Player(
            id = id,
            name = hash.getValue(NAME),
            level = hash.getValue(LEVEL).toInt(),
            region = hash.getValue(REGION),
            createdAt = Instant.parse(hash.getValue(CREATED_AT)),
        )
    }

    fun exists(id: String): Boolean = redis.hasKey(RedisKeys.player(id))

    /** HINCRBY player:{id} level {delta}; возвращает новый уровень. */
    fun incrementLevel(id: String, delta: Int): Long =
        redis.opsForHash<String, String>().increment(RedisKeys.player(id), LEVEL, delta.toLong())

    private fun Player.toHash() = mapOf(
        NAME to name,
        LEVEL to level.toString(),
        REGION to region,
        CREATED_AT to createdAt.toString(),
    )

    private companion object {
        const val NAME = "name"
        const val LEVEL = "level"
        const val REGION = "region"
        const val CREATED_AT = "created_at"
    }
}
