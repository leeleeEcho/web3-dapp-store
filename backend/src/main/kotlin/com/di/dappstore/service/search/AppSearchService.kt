package com.di.dappstore.service.search

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.DeleteRequest
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest
import co.elastic.clients.elasticsearch.indices.ExistsRequest
import com.di.dappstore.model.document.AppDocument
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.StringReader

private val logger = KotlinLogging.logger {}

/**
 * Elasticsearch search service for App full-text search
 */
@Service
class AppSearchService(
    private val esClient: ElasticsearchAsyncClient,
    @Qualifier("esObjectMapper") private val objectMapper: ObjectMapper
) {
    companion object {
        const val INDEX_NAME = AppDocument.INDEX_NAME
    }

    /**
     * Check if index exists and create it if not
     */
    fun createIndexIfNotExists(): Mono<Boolean> {
        return Mono.fromFuture {
            esClient.indices().exists(
                ExistsRequest.Builder().index(INDEX_NAME).build()
            )
        }.flatMap { existsResponse ->
            if (existsResponse.value()) {
                logger.info { "Elasticsearch index '$INDEX_NAME' already exists" }
                Mono.just(true)
            } else {
                createIndex()
            }
        }.onErrorResume { e ->
            logger.error(e) { "Failed to check/create index: ${e.message}" }
            Mono.just(false)
        }
    }

    /**
     * Create the index with mappings
     */
    private fun createIndex(): Mono<Boolean> {
        return Mono.fromFuture {
            esClient.indices().create(
                CreateIndexRequest.Builder()
                    .index(INDEX_NAME)
                    .withJson(StringReader(AppDocument.INDEX_MAPPING))
                    .build()
            )
        }.map { response ->
            val acknowledged = response.acknowledged()
            if (acknowledged) {
                logger.info { "Created Elasticsearch index '$INDEX_NAME'" }
            } else {
                logger.warn { "Index creation not acknowledged for '$INDEX_NAME'" }
            }
            acknowledged
        }.onErrorResume { e ->
            logger.error(e) { "Failed to create index: ${e.message}" }
            Mono.just(false)
        }
    }

    /**
     * Index an app document
     */
    fun indexApp(document: AppDocument): Mono<String> {
        return Mono.fromFuture {
            esClient.index(
                IndexRequest.Builder<AppDocument>()
                    .index(INDEX_NAME)
                    .id(document.id.toString())
                    .document(document)
                    .build()
            )
        }.map { response ->
            logger.debug { "Indexed app ${document.id}: ${response.result()}" }
            response.id()
        }.onErrorResume { e ->
            logger.error(e) { "Failed to index app ${document.id}: ${e.message}" }
            Mono.empty()
        }
    }

    /**
     * Delete an app document from the index
     */
    fun deleteApp(appId: Long): Mono<Boolean> {
        return Mono.fromFuture {
            esClient.delete(
                DeleteRequest.Builder()
                    .index(INDEX_NAME)
                    .id(appId.toString())
                    .build()
            )
        }.map { response ->
            val deleted = response.result().name == "Deleted"
            if (deleted) {
                logger.debug { "Deleted app $appId from index" }
            }
            deleted
        }.onErrorResume { e ->
            logger.error(e) { "Failed to delete app $appId: ${e.message}" }
            Mono.just(false)
        }
    }

    /**
     * Search apps by keyword with optional category filter
     * Searches across name, description, shortDescription, and developerName
     */
    fun searchApps(
        keyword: String,
        categoryId: Long? = null,
        limit: Int = 20
    ): Flux<AppDocument> {
        val searchQuery = buildSearchQuery(keyword, categoryId)

        return Mono.fromFuture {
            esClient.search(
                SearchRequest.Builder()
                    .index(INDEX_NAME)
                    .query(searchQuery)
                    .size(limit)
                    .build(),
                AppDocument::class.java
            )
        }.flatMapMany { response ->
            val hits = response.hits().hits()
            logger.debug { "Search '$keyword' returned ${hits.size} results" }
            Flux.fromIterable(hits.mapNotNull { it.source() })
        }.onErrorResume { e ->
            logger.error(e) { "Search failed for keyword '$keyword': ${e.message}" }
            Flux.empty()
        }
    }

    /**
     * Build the search query with multi-match and optional category filter
     */
    private fun buildSearchQuery(keyword: String, categoryId: Long?): Query {
        // Multi-match query across searchable fields
        val multiMatchQuery = Query.Builder()
            .multiMatch { mm ->
                mm.query(keyword)
                    .fields(
                        "name^3",           // Boost name matches
                        "shortDescription^2",
                        "description",
                        "developerName",
                        "blockchain"
                    )
                    .fuzziness("AUTO")      // Allow fuzzy matching
            }
            .build()

        // Filter for APPROVED status only
        val statusFilter = Query.Builder()
            .term { t ->
                t.field("status").value("APPROVED")
            }
            .build()

        // Optional category filter
        val categoryFilter = categoryId?.let {
            Query.Builder()
                .term { t ->
                    t.field("categoryId").value(it)
                }
                .build()
        }

        // Combine with bool query
        return Query.Builder()
            .bool { b ->
                b.must(multiMatchQuery)
                    .filter(statusFilter)
                    .apply {
                        categoryFilter?.let { filter(it) }
                    }
            }
            .build()
    }

    /**
     * Bulk index multiple app documents
     */
    fun bulkIndex(documents: List<AppDocument>): Mono<Int> {
        if (documents.isEmpty()) {
            return Mono.just(0)
        }

        return Mono.fromFuture {
            esClient.bulk { bulk ->
                documents.forEach { doc ->
                    bulk.operations { op ->
                        op.index { idx ->
                            idx.index(INDEX_NAME)
                                .id(doc.id.toString())
                                .document(doc)
                        }
                    }
                }
                bulk
            }
        }.map { response ->
            val successCount = response.items().count { it.error() == null }
            val errorCount = response.items().count { it.error() != null }
            if (errorCount > 0) {
                logger.warn { "Bulk index: $successCount succeeded, $errorCount failed" }
            } else {
                logger.info { "Bulk indexed $successCount documents" }
            }
            successCount
        }.onErrorResume { e ->
            logger.error(e) { "Bulk index failed: ${e.message}" }
            Mono.just(0)
        }
    }

    /**
     * Get document count in the index
     */
    fun getDocumentCount(): Mono<Long> {
        return Mono.fromFuture {
            esClient.count { c -> c.index(INDEX_NAME) }
        }.map { response ->
            response.count()
        }.onErrorReturn(0L)
    }
}
