# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the entire context
COPY . .

# Build the gateway service directly
RUN mvn -B clean package -pl :gateway-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# CRITICAL FIX: Copy from gateway-service target, not order-service!
COPY --from=builder /app/services/gateway-service/target/*.jar app.jar
EXPOSE 8080 
ENTRYPOINT ["java", "-jar", "app.jar"]