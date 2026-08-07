# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the entire context
COPY . .

# Build the inventory service directly
RUN mvn -B clean package -pl :inventory-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/services/inventory-service/target/*.jar app.jar
EXPOSE 8084 
ENTRYPOINT ["java", "-jar", "app.jar"]