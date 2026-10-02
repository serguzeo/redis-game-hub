package com.gamehub.config

import java.time.Duration

object RedisKeys {
    fun player(id: String) = "player:$id"                 // Hash
    fun playerCache(id: String) = "cache:player:$id"      // String (JSON)
    fun logins(id: String) = "logins:$id"                 // String (счётчик)
    fun achievements(id: String) = "achievements:$id"     // Set

    const val LEADERBOARD = "tournament:main"             // Sorted Set
    const val NOTIFICATIONS = "notifications"             // Stream
    const val NOTIFICATIONS_GROUP = "notifications-group"

    val PROFILE_CACHE_TTL: Duration = Duration.ofSeconds(60)
    val LOGINS_TTL: Duration = Duration.ofHours(24)
    val NOTIFICATIONS_RETENTION: Duration = Duration.ofDays(7)
}
