package com.gamehub.service

import com.gamehub.repository.AchievementRepository
import org.springframework.stereotype.Service

@Service
class AchievementService(private val achievements: AchievementRepository) {

    fun add(playerId: String, name: String) = achievements.add(playerId, name)

    fun has(playerId: String, name: String): Boolean = achievements.contains(playerId, name)

    fun common(playerId: String, otherPlayerId: String): Set<String> = achievements.common(playerId, otherPlayerId)
}
