package com.di.dappstore.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

@Configuration
@EnableR2dbcAuditing
class R2dbcConfig {

    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)

        val populator = CompositeDatabasePopulator()
        populator.addPopulators(
            ResourceDatabasePopulator(ClassPathResource("db/schema.sql"))
        )

        // 可选：添加初始数据
        try {
            populator.addPopulators(
                ResourceDatabasePopulator(ClassPathResource("db/data.sql"))
            )
        } catch (e: Exception) {
            // data.sql 文件不存在时忽略
        }

        initializer.setDatabasePopulator(populator)
        return initializer
    }
}
