package com.felipe.agroapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.logging.Logger;

@Component
public class DatabaseConfigLogger {
    private static final Logger logger = Logger.getLogger(DatabaseConfigLogger.class.getName());

    private final Environment environment;

    public DatabaseConfigLogger(Environment environment) {
        this.environment = environment;
    }

    @EventListener
    public void onApplicationEvent(ApplicationStartedEvent event) {
        logger.info("-------- DATABASE CONFIGURATION --------");
        logger.info("Active profiles: " + Arrays.toString(environment.getActiveProfiles()));
        logger.info("Default profiles: " + Arrays.toString(environment.getDefaultProfiles()));
        logger.info("Database URL: " + environment.getProperty("spring.datasource.url"));
        logger.info("Database Username: " + environment.getProperty("spring.datasource.username"));
        logger.info("Hibernate Dialect: " + environment.getProperty("spring.jpa.properties.hibernate.dialect"));
        logger.info("Schema Location: " + environment.getProperty("spring.sql.init.schema-locations"));
        logger.info("Data Location: " + environment.getProperty("spring.sql.init.data-locations"));
        logger.info("---------------------------------------");
    }
} 