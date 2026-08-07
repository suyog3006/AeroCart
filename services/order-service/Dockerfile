# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy EVERYTHING from root to ensure all sub-modules and parent POMs exist
COPY . .

# Build order-service and all required dependencies
RUN mvn -B clean package -pl :order-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR file
COPY --from=builder /app/services/order-service/target/*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]