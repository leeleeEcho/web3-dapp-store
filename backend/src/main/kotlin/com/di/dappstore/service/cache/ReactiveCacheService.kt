package com.di.dappstore.service.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Reactive cache service using Redis
 * Provides generic caching capabilities for the application
 */
@Service
class ReactiveCacheService(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    @Qualifier("redisObjectMapper") private val objectMapper: ObjectMapper
) {
    companion object {
        // Cache key prefixes
        const val CACHE_PREFIX = "dappstore:"

        // App cache keys
        const val KEY_FEATURED_APPS = "${CACHE_PREFIX}apps:featured"
        const val KEY_TOP_DOWNLOADED = "${CACHE_PREFIX}apps:top-downloaded:"
        const val KEY_TOP_RATED = "${CACHE_PREFIX}apps:top-rated:"
        const val KEY_LATEST_APPS = "${CACHE_PREFIX}apps:latest:"
        const val KEY_APP_DETAIL = "${CACHE_PREFIX}app:detail:"

        // Category cache keys
        const val KEY_CATEGORIES = "${CACHE_PREFIX}categories:all"

        // Default TTL values
        val TTL_FEATURED_APPS: Duration = Duration.ofMinutes(10)
        val TTL_TOP_APPS: Duration = Duration.ofMinutes(5)
        val TTL_CATEGORIES: Duration = Duration.ofHours(1)
        val TTL_APP_DETAIL: Duration = Duration.ofMinutes(30)
    }

    /**
     * Get value from cache with TypeReference (for generic types like List<T>)
     */
    fun <T> getFromCache(key: String, typeRef: TypeReference<T>): Mono<T> {
        return redisTemplate.opsForValue().get(key)
            .doOnSubscribe { logger.debug { "Cache lookup: $key" } }
            .map { value ->
                try {
                    objectMapper.convertValue(value, typeRef)
                } catch (e: Exception) {
                    logger.warn { "Cache deserialization failed for key $key: ${e.message}" }
                    throw e
                }
            }
            .doOnNext { logger.debug { "Cache HIT: $key" } }
            .doOnError { logger.warn { "Cache error for key $key: ${it.message}" } }
            .onErrorResume { Mono.empty() }
            .switchIfEmpty(Mono.defer {
                logger.debug { "Cache MISS: $key" }
                Mono.empty()
            })
    }

    /**
     * Get value from cache with Class type
     */
    fun <T> getFromCache(key: String, clazz: Class<T>): Mono<T> {
        return redisTemplate.opsForValue().get(key)
            .doOnSubscribe { logger.debug { "Cache lookup: $key" } }
            .map { value ->
                try {
                    objectMapper.convertValue(value, clazz)
                } catch (e: Exception) {
                    logger.warn { "Cache deserialization failed for key $key: ${e.message}" }
                    throw e
                }
            }
            .doOnNext { logger.debug { "Cache HIT: $key" } }
            .onErrorResume { Mono.empty() }
            .switchIfEmpty(Mono.defer {
                logger.debug { "Cache MISS: $key" }
                Mono.empty()
            })
    }

    /**
     * Put value into cache with TTL
     */
    fun <T : Any> putInCache(key: String, value: T, ttl: Duration): Mono<Boolean> {
        return redisTemplate.opsForValue().set(key, value, ttl)
            .doOnSuccess { success ->
                if (success) {
                    logger.debug { "Cached: $key (TTL: ${ttl.toMinutes()}m)" }
                } else {
                    logger.warn { "Failed to cache: $key" }
                }
            }
            .doOnError { logger.error { "Cache put error for $key: ${it.message}" } }
            .onErrorReturn(false)
    }

    /**
     * Invalidate specific cache key
     */
    fun invalidate(key: String): Mono<Boolean> {
        return redisTemplate.delete(key)
            .map { it > 0 }
            .doOnSuccess { deleted ->
                if (deleted) {
                    logger.info { "Cache invalidated: $key" }
                }
            }
            .onErrorReturn(false)
    }

    /**
     * Invalidate cache keys by pattern (e.g., "dappstore:apps:*")
     */
    fun invalidateByPattern(pattern: String): Mono<Long> {
        return redisTemplate.keys(pattern)
            .collectList()
            .flatMap { keys ->
                if (keys.isNotEmpty()) {
                    redisTemplate.delete(*keys.toTypedArray())
                        .doOnSuccess { count ->
                            logger.info { "Invalidated $count keys matching pattern: $pattern" }
                        }
                } else {
                    Mono.just(0L)
                }
            }
            .onErrorReturn(0L)
    }

    /**
     * Check if key exists in cache
     */
    fun exists(key: String): Mono<Boolean> {
        return redisTemplate.hasKey(key)
            .onErrorReturn(false)
    }

    /**
     * Get remaining TTL for a key
     */
    fun getTtl(key: String): Mono<Duration> {
        return redisTemplate.getExpire(key)
            .onErrorReturn(Duration.ZERO)
    }

    /**
     * Invalidate all app-related caches
     */
    fun invalidateAllAppCaches(): Mono<Void> {
        return Mono.`when`(
            invalidate(KEY_FEATURED_APPS),
            invalidateByPattern("${KEY_TOP_DOWNLOADED}*"),
            invalidateByPattern("${KEY_TOP_RATED}*"),
            invalidateByPattern("${KEY_LATEST_APPS}*"),
            invalidateByPattern("${KEY_APP_DETAIL}*")
        ).doOnSuccess {
            logger.info { "All app caches invalidated" }
        }
    }

    /**
     * Invalidate category cache
     */
    fun invalidateCategoryCache(): Mono<Void> {
        return invalidate(KEY_CATEGORIES)
            .then()
            .doOnSuccess {
                logger.info { "Category cache invalidated" }
            }
    }
}
