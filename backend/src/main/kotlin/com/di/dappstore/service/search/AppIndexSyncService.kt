package com.di.dappstore.service.search

import com.di.dappstore.model.document.AppDocument
import com.di.dappstore.model.entity.App
import com.di.dappstore.model.entity.AppStatus
import com.di.dappstore.repository.AppRepository
import com.di.dappstore.repository.CategoryRepository
import com.di.dappstore.repository.DeveloperRepository
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * Service for syncing app data to Elasticsearch
 * Handles initial sync at startup and individual app indexing
 */
@Service
class AppIndexSyncService(
    private val appSearchService: AppSearchService,
    private val appRepository: AppRepository,
    private val categoryRepository: CategoryRepository,
    private val developerRepository: DeveloperRepository
) {
    @Value("\${app.elasticsearch.sync-on-startup:true}")
    private var syncOnStartup: Boolean = true

    @Value("\${app.elasticsearch.enabled:true}")
    private var elasticsearchEnabled: Boolean = true

    /**
     * Initialize Elasticsearch index and sync data on application startup
     */
    @PostConstruct
    fun init() {
        if (!elasticsearchEnabled) {
            logger.info { "Elasticsearch is disabled, skipping index sync" }
            return
        }

        logger.info { "Initializing Elasticsearch index..." }
        appSearchService.createIndexIfNotExists()
            .flatMap { _ ->
                if (syncOnStartup) {
                    logger.info { "Starting full index sync..." }
                    syncAllApprovedApps()
                } else {
                    Mono.just(0)
                }
            }
            .subscribe(
                { count -> logger.info { "Elasticsearch initialization complete. Indexed $count apps." } },
                { error -> logger.error(error) { "Elasticsearch initialization failed" } }
            )
    }

    /**
     * Sync all approved apps to Elasticsearch
     * Used for initial population or full re-index
     */
    fun syncAllApprovedApps(): Mono<Int> {
        return appRepository.findByStatus(AppStatus.APPROVED)
            .flatMap { app -> buildAppDocument(app) }
            .collectList()
            .flatMap { documents ->
                if (documents.isEmpty()) {
                    logger.info { "No approved apps to index" }
                    Mono.just(0)
                } else {
                    logger.info { "Bulk indexing ${documents.size} apps..." }
                    appSearchService.bulkIndex(documents)
                }
            }
    }

    /**
     * Index a single app (used after app creation/update)
     */
    fun indexApp(app: App): Mono<String> {
        if (!elasticsearchEnabled) {
            return Mono.just("disabled")
        }

        // Only index approved apps
        if (app.status != AppStatus.APPROVED) {
            logger.debug { "Skipping index for non-approved app ${app.id}" }
            return Mono.just("skipped")
        }

        return buildAppDocument(app)
            .flatMap { document ->
                appSearchService.indexApp(document)
            }
    }

    /**
     * Remove an app from the index (used after app deletion or status change)
     */
    fun deleteFromIndex(appId: Long): Mono<Boolean> {
        if (!elasticsearchEnabled) {
            return Mono.just(true)
        }

        return appSearchService.deleteApp(appId)
    }

    /**
     * Re-index a single app by ID (fetches latest data from database)
     */
    fun reindexApp(appId: Long): Mono<String> {
        if (!elasticsearchEnabled) {
            return Mono.just("disabled")
        }

        return appRepository.findById(appId)
            .flatMap { app ->
                if (app.status == AppStatus.APPROVED && !app.isDeleted) {
                    indexApp(app)
                } else {
                    // Remove from index if not approved
                    deleteFromIndex(appId).thenReturn("deleted")
                }
            }
            .defaultIfEmpty("not_found")
    }

    /**
     * Build AppDocument from App entity with category and developer names
     */
    private fun buildAppDocument(app: App): Mono<AppDocument> {
        val categoryMono = app.categoryId?.let { categoryId ->
            categoryRepository.findById(categoryId)
                .map { it.displayName }
                .defaultIfEmpty("")
        } ?: Mono.just("")

        val developerMono = developerRepository.findById(app.developerId)
            .map { it.companyName ?: "" }
            .defaultIfEmpty("")

        return Mono.zip(categoryMono, developerMono)
            .map { tuple ->
                AppDocument.fromApp(
                    app = app,
                    categoryName = tuple.t1.ifEmpty { null },
                    developerName = tuple.t2.ifEmpty { null }
                )
            }
    }

    /**
     * Get index statistics
     */
    fun getIndexStats(): Mono<Map<String, Any>> {
        return appSearchService.getDocumentCount()
            .map { count ->
                mapOf(
                    "indexName" to AppDocument.INDEX_NAME,
                    "documentCount" to count,
                    "enabled" to elasticsearchEnabled
                )
            }
    }
}
