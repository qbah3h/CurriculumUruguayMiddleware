package com.curriculum.CurriculumUruguay.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration class to conditionally enable or disable database connectivity.
 * When spring.datasource.enabled=false, database auto-configuration is disabled.
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.enabled", havingValue = "true", matchIfMissing = false)
@Import({ DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class DataSourceConfig {
    // This class enables conditional database configuration
}
