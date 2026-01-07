package com.di.dappstore.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * MinIO configuration properties
 */
@ConfigurationProperties(prefix = "minio")
data class MinioProperties(
    val endpoint: String = "http://localhost:9100",
    val accessKey: String = "minioadmin",
    val secretKey: String = "minioadmin"
)
