package com.bloodbridge.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class to enable JPA auditing features in the application.
 * This automatically tracks entity creation and modification times.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
