package com.bloodbridge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties bean for Phase 3D.1 Smart Donor Matching Engine.
 */
@Configuration
@ConfigurationProperties(prefix = "matching")
@Getter
@Setter
public class MatchingConfig {

    private Radius radius = new Radius();
    private Batch batch = new Batch();
    private int minimumAcceptances = 1;
    private int cooldownDays = 90;

    @Getter
    @Setter
    public static class Radius {
        private double primary = 50.0;
        private double secondary = 75.0;
        private double tertiary = 100.0;
    }

    @Getter
    @Setter
    public static class Batch {
        private int size = 10;
    }
}
