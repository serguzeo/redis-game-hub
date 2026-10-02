package com.gamehub.service

import com.gamehub.model.LeaderboardEntry
import com.gamehub.model.NotFoundException
import com.gamehub.repository.LeaderboardRepository
import org.springframework.stereotype.Service

@Service
class LeaderboardService(private val leaderboard: LeaderboardRepository) {

    fun addScore(playerId: String, score: Double): Double = leaderboard.incrementScore(playerId, score)

    fun top(limit: Int): List<LeaderboardEntry> = leaderboard.top(limit)

    fun rank(playerId: String): LeaderboardEntry =
        leaderboard.findEntry(playerId) ?: throw NotFoundException("Player '$playerId' is not in the leaderboard")
}
