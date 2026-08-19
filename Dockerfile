# --- Build ---
FROM amazoncorretto:21-alpine AS builder

WORKDIR /app

# download the dependencies (keeping in cache)
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw package -DskipTests

# --- Runtime ---
FROM amazoncorretto:21-alpine

WORKDIR /app

# non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# only JAR compiled with permissions
COPY --chown=appuser:appgroup --from=builder /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]