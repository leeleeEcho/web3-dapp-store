package com.di.dappstore.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health", description = "健康检查接口")
class HealthController {

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    fun health(): Mono<Map<String, Any>> {
        return Mono.just(
            mapOf(
                "status" to "UP",
                "service" to "dappstore-backend",
                "timestamp" to LocalDateTime.now().toString()
            )
        )
    }

    @GetMapping("/version")
    @Operation(summary = "获取版本", description = "获取服务版本信息")
    fun version(): Mono<Map<String, String>> {
        return Mono.just(
            mapOf(
                "version" to "0.0.1-SNAPSHOT",
                "name" to "DApp Store Backend",
                "framework" to "Spring Boot 3.2.1"
            )
        )
    }
}
