# Use OpenJDK 21 as base image (matches your Java version)
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Set timezone to IST
ENV TZ=Asia/Kolkata
RUN apt-get update && apt-get install -y \
    curl \
    tzdata \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the JAR file (using the correct filename from your project)
COPY target/backend-*.jar app.jar

# Create logs directory
RUN mkdir -p /var/log/brideside-backend && \
    chown -R appuser:appuser /var/log/brideside-backend

# Change ownership of the app directory
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for production with timezone
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Djava.security.egd=file:/dev/./urandom -Duser.timezone=Asia/Kolkata"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
