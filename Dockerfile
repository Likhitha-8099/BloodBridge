# ── Stage 1: Build using official Maven with Java 21 ─────────────────────────
FROM maven:3.9.9-eclipse-temurin-21-jammy AS builder

WORKDIR /app

# Copy application source code
COPY . .

# Build Spring Boot JAR directly using pre-installed Maven
RUN if [ -f "./backend/pom.xml" ]; then \
        cd backend && mvn clean package -DskipTests -B; \
    else \
        mvn clean package -DskipTests -B; \
    fi

# Locate and stage the generated executable JAR
RUN find . -name "blood-bridge-0.0.1-SNAPSHOT.jar" -exec cp {} /app/app.jar \;

# ── Stage 2: Minimal Production JRE Runtime ──────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root system user for secure container execution
RUN groupadd -r bloodbridge && \
    useradd -r -g bloodbridge -d /app -s /sbin/nologin bloodbridge

# Copy compiled JAR from builder stage with correct permissions
COPY --from=builder \
    --chown=bloodbridge:bloodbridge \
    /app/app.jar \
    app.jar

USER bloodbridge:bloodbridge

EXPOSE 8083 10000

ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar app.jar"]
