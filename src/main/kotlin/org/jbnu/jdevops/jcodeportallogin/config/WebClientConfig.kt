package org.jbnu.jdevops.jcodeportallogin.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.unit.DataSize
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Value("\${watcher.url}")
    private lateinit var watcherUrl: String

    @Value("\${generator.url}")
    private lateinit var generatorUrl: String

    @Value("\${spring.codec.max-in-memory-size}")
    private lateinit var maxInMemorySize: DataSize

    @Bean
    fun watcherWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(watcherUrl)
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(maxInMemorySize.toBytes().toInt())
            }
            .build()
    }

    @Bean
    fun generatorWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(generatorUrl)
            .build()
    }
}


