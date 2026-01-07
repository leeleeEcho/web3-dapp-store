package com.di.dappstore.service

import com.di.dappstore.model.vo.AppListItem
import com.di.dappstore.model.vo.CategorySummary
import com.di.dappstore.service.cache.ReactiveCacheService
import com.di.dappstore.service.cache.ReactiveCacheService.Companion.KEY_CATEGORIES
import com.di.dappstore.service.cache.ReactiveCacheService.Companion.KEY_FEATURED_APPS
import com.di.dappstore.service.cache.ReactiveCacheService.Companion.KEY_LATEST_APPS
import com.di.dappstore.service.cache.ReactiveCacheService.Companion.KEY_TOP_DOWNLOADED
import com.di.dappstore.service.cache.ReactiveCacheService.Companion.KEY_TOP_RATED
import com.di.dappstore.service.cache.ReactiveCacheService.Companion.TTL_CATEGORIES
import com.di.dappstore.service.cache.ReactiveCacheService.Companion.TTL_FEATURED_APPS
import com.di.dappstore.service.cache.ReactiveCacheService.Companion.TTL_TOP_APPS
import com.fasterxml.jackson.core.type.TypeReference
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * Cached wrapper for AppService and CategoryService
 * Provides caching for frequently accessed data
 */
@Service
class CachedAppService(
    private val appService: AppService,
    private val categoryService: CategoryService,
    private val cacheService: ReactiveCacheService
) {
    // Type references for JSON deserialization
    private val appListTypeRef = object : TypeReference<List<AppListItem>>() {}
    private val categoryListTypeRef = object : TypeReference<List<CategorySummary>>() {}

    /**
     * Get featured apps with caching (TTL: 10 minutes)
     */
    fun getFeaturedApps(): Flux<AppListItem> {
        return cacheService.getFromCache(KEY_FEATURED_APPS, appListTypeRef)
            .flatMapMany { cached ->
                logger.debug { "Returning ${cached.size} featured apps from cache" }
                Flux.fromIterable(cached)
            }
            .switchIfEmpty(
                appService.getFeaturedApps()
                    .collectList()
                    .flatMap { apps ->
                        logger.debug { "Caching ${apps.size} featured apps" }
                        cacheService.putInCache(KEY_FEATURED_APPS, apps, TTL_FEATURED_APPS)
                            .thenReturn(apps)
                    }
                    .flatMapMany { Flux.fromIterable(it) }
            )
    }

    /**
     * Get top downloaded apps with caching (TTL: 5 minutes)
     */
    fun getTopDownloaded(limit: Int = 20): Flux<AppListItem> {
        val cacheKey = "$KEY_TOP_DOWNLOADED$limit"
        return cacheService.getFromCache(cacheKey, appListTypeRef)
            .flatMapMany { cached ->
                logger.debug { "Returning ${cached.size} top downloaded apps from cache" }
                Flux.fromIterable(cached)
            }
            .switchIfEmpty(
                appService.getTopDownloaded(limit)
                    .collectList()
                    .flatMap { apps ->
                        logger.debug { "Caching ${apps.size} top downloaded apps" }
                        cacheService.putInCache(cacheKey, apps, TTL_TOP_APPS)
                            .thenReturn(apps)
                    }
                    .flatMapMany { Flux.fromIterable(it) }
            )
    }

    /**
     * Get top rated apps with caching (TTL: 5 minutes)
     */
    fun getTopRated(limit: Int = 20): Flux<AppListItem> {
        val cacheKey = "$KEY_TOP_RATED$limit"
        return cacheService.getFromCache(cacheKey, appListTypeRef)
            .flatMapMany { cached ->
                logger.debug { "Returning ${cached.size} top rated apps from cache" }
                Flux.fromIterable(cached)
            }
            .switchIfEmpty(
                appService.getTopRated(limit)
                    .collectList()
                    .flatMap { apps ->
                        logger.debug { "Caching ${apps.size} top rated apps" }
                        cacheService.putInCache(cacheKey, apps, TTL_TOP_APPS)
                            .thenReturn(apps)
                    }
                    .flatMapMany { Flux.fromIterable(it) }
            )
    }

    /**
     * Get latest apps with caching (TTL: 5 minutes)
     */
    fun getLatestApps(limit: Int = 20): Flux<AppListItem> {
        val cacheKey = "$KEY_LATEST_APPS$limit"
        return cacheService.getFromCache(cacheKey, appListTypeRef)
            .flatMapMany { cached ->
                logger.debug { "Returning ${cached.size} latest apps from cache" }
                Flux.fromIterable(cached)
            }
            .switchIfEmpty(
                appService.getLatestApps(limit)
                    .collectList()
                    .flatMap { apps ->
                        logger.debug { "Caching ${apps.size} latest apps" }
                        cacheService.putInCache(cacheKey, apps, TTL_TOP_APPS)
                            .thenReturn(apps)
                    }
                    .flatMapMany { Flux.fromIterable(it) }
            )
    }

    /**
     * Get all categories with caching (TTL: 1 hour)
     */
    fun getAllCategories(): Flux<CategorySummary> {
        return cacheService.getFromCache(KEY_CATEGORIES, categoryListTypeRef)
            .flatMapMany { cached ->
                logger.debug { "Returning ${cached.size} categories from cache" }
                Flux.fromIterable(cached)
            }
            .switchIfEmpty(
                categoryService.getAllCategories()
                    .collectList()
                    .flatMap { categories ->
                        logger.debug { "Caching ${categories.size} categories" }
                        cacheService.putInCache(KEY_CATEGORIES, categories, TTL_CATEGORIES)
                            .thenReturn(categories)
                    }
                    .flatMapMany { Flux.fromIterable(it) }
            )
    }

    /**
     * Invalidate all app-related caches
     * Call this when apps are created, updated, or deleted
     */
    fun invalidateAppCaches(): Mono<Void> {
        return cacheService.invalidateAllAppCaches()
    }

    /**
     * Invalidate category cache
     * Call this when categories are created, updated, or deleted
     */
    fun invalidateCategoryCache(): Mono<Void> {
        return cacheService.invalidateCategoryCache()
    }
}
