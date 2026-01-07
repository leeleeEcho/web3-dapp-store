package com.di.dappstore.config

import com.di.dappstore.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange { exchanges ->
                exchanges
                    // 公开接口 - 认证相关
                    .pathMatchers("/api/v1/auth/**").permitAll()
                    .pathMatchers("/api/v1/health", "/api/v1/version").permitAll()

                    // 公开接口 - 只读 GET 请求
                    .pathMatchers(HttpMethod.GET, "/api/v1/apps/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/v1/developers/**").permitAll()

                    // Swagger/OpenAPI
                    .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/webjars/**").permitAll()

                    // H2 Console (开发环境)
                    .pathMatchers("/h2-console/**").permitAll()

                    // Actuator
                    .pathMatchers("/actuator/**").permitAll()

                    // 需要认证的接口
                    .pathMatchers(HttpMethod.POST, "/api/v1/apps/*/reviews").authenticated()
                    .pathMatchers(HttpMethod.POST, "/api/v1/developers/**").authenticated()
                    .pathMatchers(HttpMethod.PUT, "/api/v1/developers/**").authenticated()
                    .pathMatchers("/api/v1/storage/**").authenticated()

                    // 管理员接口
                    .pathMatchers("/api/v1/admin/**").hasRole("ADMIN")

                    // 其他接口需要认证
                    .anyExchange().authenticated()
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
