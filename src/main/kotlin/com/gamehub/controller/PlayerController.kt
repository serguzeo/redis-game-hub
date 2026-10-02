package com.gamehub.controller

import com.gamehub.dto.BatchCreateRequest
import com.gamehub.dto.BatchCreateResponse
import com.gamehub.dto.LevelChangeRequest
import com.gamehub.dto.LevelResponse
import com.gamehub.dto.LoginResponse
import com.gamehub.dto.PlayerRequest
import com.gamehub.model.Player
import com.gamehub.service.PlayerService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/players")
class PlayerController(private val playerService: PlayerService) {

    @PostMapping("/{id}")
    fun upsert(@PathVariable id: String, @Valid @RequestBody request: PlayerRequest): Player =
        playerService.upsert(id, request.name, request.level, request.region)

    /** Заголовок X-Cache показывает, откуда взят профиль: HIT — из кеша, MISS — из Hash. */
    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ResponseEntity<Player> {
        val lookup = playerService.get(id)
        return ResponseEntity.ok()
            .header("X-Cache", if (lookup.cacheHit) "HIT" else "MISS")
            .body(lookup.player)
    }

    @PatchMapping("/{id}/level")
    fun changeLevel(@PathVariable id: String, @RequestBody request: LevelChangeRequest) =
        LevelResponse(id, playerService.changeLevel(id, request.delta))

    @PostMapping("/{id}/login")
    fun login(@PathVariable id: String) = LoginResponse(id, playerService.registerLogin(id))

    @PostMapping("/batch")
    fun createBatch(@Valid @RequestBody request: BatchCreateRequest): BatchCreateResponse {
        val now = playerService.now()
        val players = request.players.map { Player(it.id, it.name, it.level, it.region, now) }
        return BatchCreateResponse(players.size, playerService.createBatch(players))
    }
}
