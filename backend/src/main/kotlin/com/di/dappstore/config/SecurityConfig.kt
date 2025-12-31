package com.di.dappstore.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    // 公开接口
                    .pathMatchers("/api/v1/health", "/api/v1/version").permitAll()
                    .pathMatchers("/api/v1/auth/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/v1/apps/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/v1/developers/**").permitAll()
                    // Swagger/OpenAPI
                    .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/webjars/**").permitAll()
                    // H2 Console (开发环境)
                    .pathMatchers("/h2-console/**").permitAll()
                    // Actuator
                    .pathMatchers("/actuator/**").permitAll()
                    // 其他接口需要认证
                    .anyExchange().permitAll()  // 暂时全部放开，后续添加 JWT 认证后改为 authenticated()
            }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization", "Content-Type")
            allowCredentials = true
            maxAge = 3600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
