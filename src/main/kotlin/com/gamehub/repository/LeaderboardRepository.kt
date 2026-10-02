package com.gamehub.repository

import com.gamehub.config.RedisKeys
import com.gamehub.model.LeaderboardEntry
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository

/** Лидерборд турнира: Sorted Set `tournament:main` (member = player_id, score = очки). */
@Repository
class LeaderboardRepository(private val redis: StringRedisTemplate) {

    private val zset get() = redis.opsForZSet()

    /** ZINCRBY tournament:main {score} {player_id}; возвращает итоговые очки. */
    fun incrementScore(playerId: String, score: Double): Double =
        zset.incrementScore(RedisKeys.LEADERBOARD, playerId, score) ?: score

    /** ZREVRANGE tournament:main 0 {limit-1} WITHSCORES */
    fun top(limit: Int): List<LeaderboardEntry> =
        zset.reverseRangeWithScores(RedisKeys.LEADERBOARD, 0, limit - 1L)
            .orEmpty()
            .mapIndexed { index, tuple -> LeaderboardEntry(tuple.value!!, tuple.score!!, index + 1L) }

    /**
     * Место игрока. Лидерборд отсортирован по убыванию очков, поэтому используем
     * ZREVRANK (ZRANK дал бы позицию в порядке возрастания). Место = индекс + 1.
     */
    fun findEntry(playerId: String): LeaderboardEntry? {
        val rank = zset.reverseRank(RedisKeys.LEADERBOARD, playerId) ?: return null
        val score = zset.score(RedisKeys.LEADERBOARD, playerId) ?: return null
        return LeaderboardEntry(playerId, score, rank + 1)
    }
}
