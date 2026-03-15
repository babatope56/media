package com.media.media.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * DataSource configuration for H2 in-memory database.
 * 
 * The H2 database is configured as an in-memory database for development and testing purposes.
 * Access the H2 console at: http://localhost:8080/h2-console
 * 
 * Default credentials:
 * - Driver Class: org.h2.Driver
 * - JDBC URL: jdbc:h2:mem:testdb
 * - User Name: sa
 * - Password: (empty)
 */
@Configuration
public class DataSourceConfig {
    
    /**
     * Creates the H2 DataSource bean for the application.
     * This uses an in-memory database that persists only during runtime.
     *
     * @return DataSource configured for H2
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false")
                .username("sa")
                .password("")
                .build();
    }
}

