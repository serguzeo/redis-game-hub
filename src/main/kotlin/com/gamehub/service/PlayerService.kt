package com.gamehub.service

import com.gamehub.model.NotFoundException
import com.gamehub.model.Notification
import com.gamehub.model.NotificationType
import com.gamehub.model.Player
import com.gamehub.repository.LoginCounterRepository
import com.gamehub.repository.NotificationRepository
import com.gamehub.repository.PlayerCacheRepository
import com.gamehub.repository.PlayerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.time.measureTime

/** Результат чтения профиля: сам профиль и признак попадания в кеш. */
data class PlayerLookup(val player: Player, val cacheHit: Boolean)

@Service
class PlayerService(
    private val players: PlayerRepository,
    private val cache: PlayerCacheRepository,
    private val logins: LoginCounterRepository,
    private val notifications: NotificationRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /** Создание или обновление профиля. created_at сохраняется с момента первого создания. */
    fun upsert(id: String, name: String, level: Int, region: String): Player {
        val existing = players.findById(id)
        val player = Player(id, name, level, region, createdAt = existing?.createdAt ?: now())
        players.save(player)
        cache.evict(id)
        notify(id, NotificationType.PROFILE_UPDATED, if (existing == null) "Profile created" else "Profile updated")
        return player
    }

    /**
     * Cache-Aside: сначала кеш, при промахе — Hash профиля, затем запись в кеш с TTL.
     */
    fun get(id: String): PlayerLookup {
        cache.get(id)?.let {
            log.debug("Cache HIT for player {}", id)
            return PlayerLookup(it, cacheHit = true)
        }
        log.debug("Cache MISS for player {}", id)
        val player = players.findById(id) ?: throw NotFoundException("Player '$id' not found")
        // Кеш — вспомогательный: если записать не удалось (например, мастер отклоняет записи
        // из-за min-replicas-to-write), всё равно отдаём профиль.
        runCatching { cache.put(player) }
            .onFailure { log.warn("Could not cache player {}: {}", id, it.message) }
        return PlayerLookup(player, cacheHit = false)
    }

    /** HINCRBY уровня + инвалидация кеша + уведомление в поток. */
    fun changeLevel(id: String, delta: Int): Long {
        requireExists(id)
        val newLevel = players.incrementLevel(id, delta)
        cache.evict(id)
        val sign = if (delta >= 0) "+" else ""
        notify(id, NotificationType.LEVEL_CHANGED, "Level changed by $sign$delta, now $newLevel")
        return newLevel
    }

    /** Фиксирует вход игрока, возвращает количество входов за сутки. */
    fun registerLogin(id: String): Long {
        requireExists(id)
        return logins.increment(id)
    }

    /** Массовое создание профилей через pipeline. Возвращает время выполнения в мс. */
    fun createBatch(batch: List<Player>): Long {
        val elapsed = measureTime {
            players.saveAll(batch)
        }
        cache.evict(*batch.map { it.id }.toTypedArray())
        log.info("Batch of {} players saved via pipeline in {}", batch.size, elapsed)
        return elapsed.inWholeMilliseconds
    }

    fun now(): Instant = Instant.now()

    private fun requireExists(id: String) {
        if (!players.exists(id)) throw NotFoundException("Player '$id' not found")
    }

    private fun notify(playerId: String, type: NotificationType, message: String) {
        notifications.publish(Notification(playerId, type, message, now()))
    }
}
