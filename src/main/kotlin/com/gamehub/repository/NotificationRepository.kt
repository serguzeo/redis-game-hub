package com.gamehub.repository

import com.gamehub.config.RedisKeys
import com.gamehub.model.Notification
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Instant

/** Очередь уведомлений: Redis Stream `notifications`. */
@Repository
class NotificationRepository(private val redis: StringRedisTemplate) {

    /**
     * XADD notifications * player_id .. type .. message .. timestamp ..
     * Затем XTRIM notifications MINID ~ {now - 7 дней}: ID записи начинается с unix-времени в мс,
     * поэтому так удаляются сообщения старше 7 дней.
     */
    fun publish(notification: Notification) {
        val fields = mapOf(
            "player_id" to notification.playerId,
            "type" to notification.type.name,
            "message" to notification.message,
            "timestamp" to notification.timestamp.toString(),
        )
        redis.opsForStream<String, String>().add(StreamRecords.string(fields).withStreamKey(RedisKeys.NOTIFICATIONS))

        val minId = Instant.now().minus(RedisKeys.NOTIFICATIONS_RETENTION).toEpochMilli().toString()
        redis.execute { it.execute("XTRIM", *listOf(RedisKeys.NOTIFICATIONS, "MINID", "~", minId).map(String::toByteArray).toTypedArray()) }
    }

    /** XGROUP CREATE notifications notifications-group $ MKSTREAM (если группы ещё нет). */
    fun createConsumerGroupIfAbsent() {
        val exists = redis.hasKey(RedisKeys.NOTIFICATIONS) &&
            redis.opsForStream<String, String>().groups(RedisKeys.NOTIFICATIONS).any { it.groupName() == RedisKeys.NOTIFICATIONS_GROUP }
        if (exists) return
        redis.execute {
            it.streamCommands().xGroupCreate(RedisKeys.NOTIFICATIONS.toByteArray(), RedisKeys.NOTIFICATIONS_GROUP, ReadOffset.latest(), true)
        }
    }
}
