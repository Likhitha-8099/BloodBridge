# ==============================================================================
# Build Stage: Compile and Package BloodBridge using Java 21 & Maven Wrapper
# ==============================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy Maven wrapper configuration and descriptors
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Normalize line endings and ensure executable permissions for Linux
RUN sed -i 's/\r$//' ./mvnw && chmod +x ./mvnw

# Download dependencies offline if possible (caching layer)
RUN ./mvnw dependency:go-offline -B || true

# Copy backend source code
COPY src/ ./src/

# Package Spring Boot application skipping tests for deployment
RUN ./mvnw clean package -DskipTests -B

# ==============================================================================
# Runtime Stage: Lightweight, Production-Ready Java 21 JRE Container
# ==============================================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create a dedicated non-root service user for enhanced container security
RUN groupadd -r bloodbridge && useradd -r -g bloodbridge -d /app -s /sbin/nologin bloodbridge

# Copy the generated Spring Boot executable JAR from the builder stage
COPY --from=builder --chown=bloodbridge:bloodbridge /app/target/blood-bridge-0.0.1-SNAPSHOT.jar app.jar

# Set container permissions
USER bloodbridge:bloodbridge

# Render supplies the PORT environment variable dynamically at runtime (defaults to 8083 locally)
ENV PORT=8083
EXPOSE 8083

# Launch Spring Boot with environment variable resolution and container memory awareness
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
