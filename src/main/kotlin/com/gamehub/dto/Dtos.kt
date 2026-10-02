package com.gamehub.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class PlayerRequest(
    @field:NotBlank val name: String,
    @field:Min(1) val level: Int = 1,
    @field:NotBlank val region: String,
)

data class BatchPlayerRequest(
    @field:NotBlank val id: String,
    @field:NotBlank val name: String,
    @field:Min(1) val level: Int = 1,
    @field:NotBlank val region: String,
)

data class BatchCreateRequest(@field:NotEmpty @field:Valid val players: List<BatchPlayerRequest>)

data class BatchCreateResponse(val created: Int, val elapsedMs: Long)

data class LevelChangeRequest(val delta: Int)

data class LevelResponse(val playerId: String, val level: Long)

data class LoginResponse(val playerId: String, val loginsToday: Long)

data class ScoreRequest(@field:NotBlank val playerId: String, val score: Double)

data class ScoreResponse(val playerId: String, val totalScore: Double)

data class AchievementRequest(@field:NotBlank val name: String)

data class AchievementResponse(val playerId: String, val name: String, val present: Boolean)

data class CommonAchievementsResponse(val playerIds: List<String>, val common: Set<String>)

data class ErrorResponse(val status: Int, val message: String?)
