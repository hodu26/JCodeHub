package org.jbnu.jdevops.jcodeportallogin.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Value("\${watcher.url}")
    private lateinit var watcherUrl: String

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl(watcherUrl)
            .build()
    }
}

