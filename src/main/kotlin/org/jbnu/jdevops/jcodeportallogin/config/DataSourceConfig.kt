package org.jbnu.jdevops.jcodeportallogin.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import javax.sql.DataSource


@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
@Configuration
class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.master.hikari")
    fun masterDataSource(): DataSource {
        return DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica.hikari")
    fun replicaDataSource(): DataSource {
        return DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()
    }
//
    @Bean
    fun routingDataSource(): DataSource {
        val routingDataSource = RoutingDataSource()
        val dataSourceMap = HashMap<Any, Any>()
        dataSourceMap[DataSourceKey.MASTER] = masterDataSource()
        dataSourceMap[DataSourceKey.REPLICA] = replicaDataSource()

        routingDataSource.setTargetDataSources(dataSourceMap)
        // 기본 데이터 소스는 master로 설정
        routingDataSource.setDefaultTargetDataSource(masterDataSource())
        // targetDataSources가 확실히 설정되도록 초기화 호출
        routingDataSource.afterPropertiesSet()

        return routingDataSource
    }

    @Bean
    fun entityManagerDataSource(): DataSource {
        // 실제 RoutingDataSource를 반환하여 Hibernate가 필요한 메타데이터를 읽을 수 있도록 함
        return routingDataSource()
    }

    @Primary
    @Bean("dataSource")
    fun dataSource(routingDataSource: DataSource): DataSource {
        return LazyConnectionDataSourceProxy(routingDataSource)
    }

}
