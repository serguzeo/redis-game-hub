package com.gamehub.controller

import com.gamehub.dto.ScoreRequest
import com.gamehub.dto.ScoreResponse
import com.gamehub.service.LeaderboardService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/leaderboard")
class LeaderboardController(private val leaderboardService: LeaderboardService) {

    @PostMapping("/score")
    fun addScore(@Valid @RequestBody request: ScoreRequest) =
        ScoreResponse(request.playerId, leaderboardService.addScore(request.playerId, request.score))

    @GetMapping("/top")
    fun top(@RequestParam(defaultValue = "10") @Min(1) @Max(100) limit: Int) = leaderboardService.top(limit)

    @GetMapping("/rank/{playerId}")
    fun rank(@PathVariable playerId: String) = leaderboardService.rank(playerId)
}
