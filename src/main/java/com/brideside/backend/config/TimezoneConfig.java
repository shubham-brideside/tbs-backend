package com.brideside.backend.config;

import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
public class TimezoneConfig {

    private static final Logger logger = LoggerFactory.getLogger(TimezoneConfig.class);

    @PostConstruct
    public void init() {
        // Set the default timezone to IST - this must be done early
        TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
        TimeZone.setDefault(istTimeZone);
        
        // Set system property as backup (must be set before any date/time operations)
        System.setProperty("user.timezone", "Asia/Kolkata");
        
        // Verify the timezone was set correctly
        TimeZone currentTimeZone = TimeZone.getDefault();
        String systemProperty = System.getProperty("user.timezone");
        
        logger.info("========================================");
        logger.info("TIMEZONE CONFIGURATION:");
        logger.info("Default TimeZone: {} (ID: {})", currentTimeZone.getDisplayName(), currentTimeZone.getID());
        logger.info("System Property user.timezone: {}", systemProperty);
        logger.info("Expected TimeZone ID: Asia/Kolkata");
        logger.info("Expected Offset: +05:30 (IST)");
        logger.info("Current Offset: {}", formatOffset(currentTimeZone.getRawOffset()));
        logger.info("========================================");
        
        // Verify it's actually IST
        if (!"Asia/Kolkata".equals(currentTimeZone.getID())) {
            logger.error("WARNING: TimeZone is NOT set to Asia/Kolkata! Current: {}", currentTimeZone.getID());
        } else {
            logger.info("✓ TimeZone correctly set to IST (Asia/Kolkata)");
        }
    }
    
    private String formatOffset(int offsetMs) {
        int offsetHours = offsetMs / (1000 * 60 * 60);
        int offsetMinutes = Math.abs((offsetMs % (1000 * 60 * 60)) / (1000 * 60));
        return String.format("%+03d:%02d", offsetHours, offsetMinutes);
    }
}
