package com.gamehub.model

import java.time.Instant

data class Player(
    val id: String,
    val name: String,
    val level: Int,
    val region: String,
    val createdAt: Instant,
)
