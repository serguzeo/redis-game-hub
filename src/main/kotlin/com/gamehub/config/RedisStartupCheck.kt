package com.gamehub.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisStartupCheck(private val redis: StringRedisTemplate) : InitializingBean {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun afterPropertiesSet() {
        val retry = RetryTemplate(checkRedisRetryPolicy)
        retry.invoke { redis.execute { it.ping() } }
        log.info("Redis is reachable via Sentinel")
    }

    companion object {
        private val checkRedisRetryPolicy = RetryPolicy
            .builder()
            .maxRetries(10)
            .delay(Duration.ofSeconds(3))
            .build()
    }
}
