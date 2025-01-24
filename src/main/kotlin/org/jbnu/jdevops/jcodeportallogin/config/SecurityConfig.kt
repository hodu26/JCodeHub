package org.jbnu.jdevops.jcodeportallogin.config

import jakarta.servlet.http.HttpSessionEvent
import jakarta.servlet.http.HttpSessionListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter

@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/api/auth/signup","api/auth/login/basic", "/login/oidc/success", "/error", "/oauth2/**").permitAll()
                    .anyRequest().authenticated()  // 모든 요청에 대해 인증 요구
            }
            .oauth2Login { oauth2 ->
                oauth2.defaultSuccessUrl("/api/auth/login/oidc/success", true)  // 인증 성공 후 이동할 URL 설정
            }
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