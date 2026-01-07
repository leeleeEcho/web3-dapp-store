package com.di.dappstore.config

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Elasticsearch configuration for async client
 */
@Configuration
class ElasticsearchConfig {

    @Value("\${spring.elasticsearch.uris:http://localhost:9200}")
    private lateinit var elasticsearchUris: String

    /**
     * ObjectMapper for Elasticsearch JSON serialization
     */
    @Bean
    fun esObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    /**
     * Elasticsearch RestClient (low-level client)
     */
    @Bean
    fun elasticsearchRestClient(): RestClient {
        val hosts = elasticsearchUris.split(",").map { uri ->
            val url = java.net.URL(uri.trim())
            HttpHost(url.host, url.port, url.protocol)
        }.toTypedArray()

        return RestClient.builder(*hosts)
            .setRequestConfigCallback { config ->
                config
                    .setConnectTimeout(5000)
                    .setSocketTimeout(60000)
            }
            .build()
    }

    /**
     * Elasticsearch AsyncClient for reactive operations
     */
    @Bean
    fun elasticsearchAsyncClient(
        restClient: RestClient,
        esObjectMapper: ObjectMapper
    ): ElasticsearchAsyncClient {
        val transport = RestClientTransport(restClient, JacksonJsonpMapper(esObjectMapper))
        return ElasticsearchAsyncClient(transport)
    }
}
