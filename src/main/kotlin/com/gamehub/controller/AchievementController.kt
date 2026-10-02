package com.gamehub.controller

import com.gamehub.dto.AchievementRequest
import com.gamehub.dto.AchievementResponse
import com.gamehub.dto.CommonAchievementsResponse
import com.gamehub.service.AchievementService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/players/{id}/achievements")
class AchievementController(private val achievementService: AchievementService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@PathVariable id: String, @Valid @RequestBody request: AchievementRequest): AchievementResponse {
        achievementService.add(id, request.name)
        return AchievementResponse(id, request.name, present = true)
    }

    @GetMapping("/{name}")
    fun has(@PathVariable id: String, @PathVariable name: String) =
        AchievementResponse(id, name, achievementService.has(id, name))

    @GetMapping("/common/{otherId}")
    fun common(@PathVariable id: String, @PathVariable otherId: String) =
        CommonAchievementsResponse(listOf(id, otherId), achievementService.common(id, otherId))
}
