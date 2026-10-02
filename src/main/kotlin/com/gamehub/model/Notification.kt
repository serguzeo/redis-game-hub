package com.gamehub.model

import java.time.Instant

enum class NotificationType { PROFILE_UPDATED, LEVEL_CHANGED }

/** Сообщение в Redis Stream `notifications`. */
data class Notification(
    val playerId: String,
    val type: NotificationType,
    val message: String,
    val timestamp: Instant,
)
