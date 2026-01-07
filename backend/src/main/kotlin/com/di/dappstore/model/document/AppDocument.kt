package com.di.dappstore.model.document

import com.di.dappstore.model.entity.App
import com.di.dappstore.model.entity.AppStatus
import java.time.LocalDateTime

/**
 * Elasticsearch document for App search
 * Contains searchable fields for full-text search
 */
data class AppDocument(
    val id: Long,
    val packageName: String,
    val name: String,
    val description: String?,
    val shortDescription: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val developerId: Long,
    val developerName: String?,
    val isWeb3: Boolean,
    val blockchain: String?,
    val downloadCount: Long,
    val ratingAverage: Double,
    val ratingCount: Long,
    val status: String,
    val isFeatured: Boolean,
    val iconUrl: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        const val INDEX_NAME = "dappstore-apps"

        /**
         * Create AppDocument from App entity with category and developer names
         */
        fun fromApp(
            app: App,
            categoryName: String? = null,
            developerName: String? = null
        ): AppDocument {
            return AppDocument(
                id = app.id!!,
                packageName = app.packageName,
                name = app.name,
                description = app.description,
                shortDescription = app.shortDescription,
                categoryId = app.categoryId,
                categoryName = categoryName,
                developerId = app.developerId,
                developerName = developerName,
                isWeb3 = app.isWeb3,
                blockchain = app.blockchain,
                downloadCount = app.downloadCount,
                ratingAverage = app.ratingAverage,
                ratingCount = app.ratingCount,
                status = app.status.name,
                isFeatured = app.isFeatured,
                iconUrl = app.iconUrl,
                createdAt = app.createdAt,
                updatedAt = app.updatedAt
            )
        }

        /**
         * Elasticsearch index mapping as JSON string
         */
        val INDEX_MAPPING = """
        {
            "mappings": {
                "properties": {
                    "id": { "type": "long" },
                    "packageName": { "type": "keyword" },
                    "name": {
                        "type": "text",
                        "analyzer": "standard",
                        "fields": {
                            "keyword": { "type": "keyword" }
                        }
                    },
                    "description": { "type": "text", "analyzer": "standard" },
                    "shortDescription": { "type": "text", "analyzer": "standard" },
                    "categoryId": { "type": "long" },
                    "categoryName": { "type": "keyword" },
                    "developerId": { "type": "long" },
                    "developerName": {
                        "type": "text",
                        "fields": {
                            "keyword": { "type": "keyword" }
                        }
                    },
                    "isWeb3": { "type": "boolean" },
                    "blockchain": { "type": "keyword" },
                    "downloadCount": { "type": "long" },
                    "ratingAverage": { "type": "double" },
                    "ratingCount": { "type": "long" },
                    "status": { "type": "keyword" },
                    "isFeatured": { "type": "boolean" },
                    "iconUrl": { "type": "keyword", "index": false },
                    "createdAt": { "type": "date" },
                    "updatedAt": { "type": "date" }
                }
            },
            "settings": {
                "number_of_shards": 1,
                "number_of_replicas": 0
            }
        }
        """.trimIndent()
    }
}
