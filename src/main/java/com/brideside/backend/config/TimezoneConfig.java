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
        // Set the default timezone to IST
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        
        // Log the timezone setting for debugging
        TimeZone currentTimeZone = TimeZone.getDefault();
        logger.info("Timezone set to: {} (ID: {})", currentTimeZone.getDisplayName(), currentTimeZone.getID());
        
        // Also set system property as backup
        System.setProperty("user.timezone", "Asia/Kolkata");
        logger.info("System property user.timezone set to: {}", System.getProperty("user.timezone"));
    }
}
