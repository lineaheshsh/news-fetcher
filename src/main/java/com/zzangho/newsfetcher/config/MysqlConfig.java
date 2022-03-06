package com.zzangho.newsfetcher.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class MysqlConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    DataSource springBatchDB() {
        DataSourceBuilder builder = DataSourceBuilder.create();
        builder.type(HikariDataSource.class);
        return builder.build();
    }

    @Bean
    PlatformTransactionManager springBatchTxManager() {
        return new DataSourceTransactionManager(springBatchDB());
    }
}
