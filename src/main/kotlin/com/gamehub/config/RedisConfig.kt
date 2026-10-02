package com.gamehub.config

import io.lettuce.core.ReadFrom
import io.lettuce.core.internal.HostAndPort
import io.lettuce.core.resource.MappingSocketAddressResolver
import org.springframework.boot.data.redis.autoconfigure.ClientResourcesBuilderCustomizer
import org.springframework.boot.data.redis.autoconfigure.LettuceClientConfigurationBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.function.Function

@Configuration
class RedisConfig {

    /**
     * Чтения отправляем на реплики, записи — на мастер. После failover Lettuce сам переключается на новый мастер.
     */
    @Bean
    fun readFromReplicas() = LettuceClientConfigurationBuilderCustomizer { it.readFrom(ReadFrom.REPLICA_PREFERRED) }

    @Bean
    @Profile("local")
    fun localAddresses() = ClientResourcesBuilderCustomizer { it ->
        it.socketAddressResolver(MappingSocketAddressResolver.create(Function { HostAndPort.of("localhost", it.port) }))
    }
}
