FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY . .

RUN sed -i 's/\r$//' ./backend/mvnw && \
    chmod +x ./backend/mvnw

RUN cd backend && \
    ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd -r bloodbridge && \
    useradd -r -g bloodbridge -d /app -s /sbin/nologin bloodbridge

COPY --from=builder \
    --chown=bloodbridge:bloodbridge \
    /app/backend/target/blood-bridge-0.0.1-SNAPSHOT.jar \
    app.jar

USER bloodbridge:bloodbridge

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar app.jar"]
