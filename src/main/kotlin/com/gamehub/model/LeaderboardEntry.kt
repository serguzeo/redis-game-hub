package com.gamehub.model

data class LeaderboardEntry(
    val playerId: String,
    val score: Double,
    val rank: Long,
)
