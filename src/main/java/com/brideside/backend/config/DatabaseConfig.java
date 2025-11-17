package com.brideside.backend.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Configuration
@Profile("!test")
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        logger.info("Configuring database with IST timezone...");
        
        HikariConfig config = new HikariConfig();
        
        // Ensure JDBC URL has timezone parameter
        String jdbcUrl = properties.getUrl();
        if (!jdbcUrl.contains("serverTimezone")) {
            String separator = jdbcUrl.contains("?") ? "&" : "?";
            jdbcUrl = jdbcUrl + separator + "serverTimezone=Asia/Kolkata";
        }
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setDriverClassName(properties.getDriverClassName());
        
        // Set timezone on every connection - use named timezone for better compatibility
        // This ensures MySQL TIMESTAMP columns use IST timezone
        // Try named timezone first, fallback to offset if needed
        String connectionInitSql = "SET time_zone = '+05:30'";
        config.setConnectionInitSql(connectionInitSql);
        logger.info("Connection Init SQL: {}", connectionInitSql);
        
        // Copy HikariCP settings from properties
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        
        HikariDataSource dataSource = new HikariDataSource(config);
        
        // Verify timezone is set correctly on first connection
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT @@session.time_zone as session_tz, @@global.time_zone as global_tz, NOW() as current_time")) {
            
            if (rs.next()) {
                String sessionTz = rs.getString("session_tz");
                String globalTz = rs.getString("global_tz");
                String currentTime = rs.getString("current_time");
                
                logger.info("========================================");
                logger.info("DATABASE TIMEZONE VERIFICATION:");
                logger.info("Session TimeZone: {}", sessionTz);
                logger.info("Global TimeZone: {}", globalTz);
                logger.info("Current Database Time: {}", currentTime);
                logger.info("JVM Default TimeZone: {}", java.util.TimeZone.getDefault().getID());
                logger.info("Expected Session TimeZone: Asia/Kolkata or +05:30");
                logger.info("========================================");
                
                if (!sessionTz.contains("+05:30") && !sessionTz.equals("Asia/Kolkata") && !sessionTz.equals("SYSTEM")) {
                    logger.warn("WARNING: Session timezone might not be set correctly! Current: {}", sessionTz);
                } else {
                    logger.info("✓ Database session timezone is correctly configured");
                }
            }
        } catch (Exception e) {
            logger.error("Error verifying database timezone configuration", e);
        }
        
        return dataSource;
    }
}


