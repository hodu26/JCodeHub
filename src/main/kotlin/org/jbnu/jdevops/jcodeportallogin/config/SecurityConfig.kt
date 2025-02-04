package org.jbnu.jdevops.jcodeportallogin.config

import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration

@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors ->
                cors.configurationSource {
                    val configuration = CorsConfiguration()
                    configuration.allowedOrigins = listOf("http://localhost:3000")
                    configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    configuration.allowedHeaders = listOf("*")
                    configuration.exposedHeaders = listOf("Authorization")
                    configuration.allowCredentials = true
                    configuration
                }
            }
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/api/user/info", "/api/user/courses", "/api/user/**").permitAll()
                    .requestMatchers("/api/user/student", "/api/user/assistant", "/api/user/professor").hasAuthority("ADMIN")
                    .requestMatchers("/api/user/**").hasAuthority("ADMIN")
                    .requestMatchers("/api/auth/signup", "/api/auth/login/basic").permitAll()
                    .requestMatchers("/oauth2/**", "/error", "/api/**").permitAll()
                    .requestMatchers("/swagger-ui/index.html", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()  // 모든 요청에 대해 인증 요구
            }
            .oauth2Login { oauth2 ->
                oauth2.defaultSuccessUrl("/api/auth/login/oidc/success", true)  // 인증 성공 후 이동할 URL 설정
            }
            .csrf { it.disable() }  // csrf 허용 ( 개발 용 )
            .logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login/basic")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            }
            .sessionManagement { sessionManagement ->
                sessionManagement.sessionFixation { sessionFixation ->
                    sessionFixation.migrateSession()  // 세션 고정 보호
                }
                sessionManagement.maximumSessions(1)  // 동시 세션 1개로 제한
            }
            .addFilterAfter(redirectFilter(), OAuth2LoginAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun redirectFilter(): CustomRedirectFilter {
        return CustomRedirectFilter()
    }

    @Bean
    fun httpSessionListener(): HttpSessionListener {
        return object : HttpSessionListener {
            override fun sessionCreated(se: HttpSessionEvent) {
                se.session.maxInactiveInterval = 3600  // 1시간 (3600초)
            }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}