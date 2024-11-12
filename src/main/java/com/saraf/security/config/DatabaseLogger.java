package com.saraf.security.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseLogger {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseLogger.class);

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @PostConstruct
    public void logDatabaseUrl() {
        logger.info("Connecting to database at URL: {}", databaseUrl);
    }
}

