# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the parent project object model
COPY pom.xml .

# Copy the entire services directory to capture all source code and POMs
COPY services/ ./services/

# Build the specific service passed or defined (e.g., gateway-service, order-service, etc.)
# Note: You can replace the hardcoded path with a build argument if using a single shared Dockerfile
RUN mvn clean package -pl :order-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Update the target jar path to match the service you are building
COPY --from=builder /app/services/order-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]