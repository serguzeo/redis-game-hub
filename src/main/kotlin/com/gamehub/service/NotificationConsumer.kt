package com.gamehub.service

import com.gamehub.config.RedisKeys
import com.gamehub.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import org.springframework.dao.DataAccessException
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Потребитель потока `notifications`: раз в секунду читает новые сообщения через XREADGROUP,
 * выводит их в лог и подтверждает через XACK.
 */
@Component
class NotificationConsumer(
    private val redis: StringRedisTemplate,
    private val notifications: NotificationRepository,
) : InitializingBean {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun afterPropertiesSet() {
        // Сразу после старта кластера реплики могут быть ещё не готовы и мастер отклоняет запись (NOREPLICAS)
        val retry = RetryTemplate(RetryPolicy.builder().maxRetries(10).delay(Duration.ofSeconds(3)).build())
        retry.invoke { notifications.createConsumerGroupIfAbsent() }
    }

    /**
     * XREADGROUP GROUP notifications-group consumer-1 COUNT 10 STREAMS notifications >
     * Если Redis недоступен (failover) или мастер отклоняет запись (NOREPLICAS), следующая попытка — через секунду.
     */
    @Scheduled(fixedDelay = 1000)
    fun poll() {
        try {
            redis.opsForStream<String, String>()
                .read(
                    Consumer.from(RedisKeys.NOTIFICATIONS_GROUP, CONSUMER_NAME),
                    StreamReadOptions.empty().count(10),
                    StreamOffset.create(RedisKeys.NOTIFICATIONS, ReadOffset.lastConsumed()),
                )
                ?.forEach(::handle)
        } catch (e: DataAccessException) {
            log.warn("Failed to read notifications: {}", e.mostSpecificCause.message)
        }
    }

    private fun handle(record: MapRecord<String, String, String>) {
        val msg = record.value
        log.info("[NOTIFICATION {}] player={} type={} message='{}'", record.id, msg["player_id"], msg["type"], msg["message"])
        redis.opsForStream<String, String>().acknowledge(RedisKeys.NOTIFICATIONS_GROUP, record)
    }

    private companion object {
        const val CONSUMER_NAME = "consumer-1"
    }
}