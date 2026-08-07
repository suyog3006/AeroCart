# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the parent pom
COPY pom.xml .

# Copy all service POMs and source directories to preserve the multi-module structure
COPY services/ ./services/

# Build the specific service with its dependencies
RUN mvn clean package -pl :notification-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/services/notification-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]