FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy all POM files first to leverage Docker layer caching
COPY pom.xml .
COPY services/gateway-service/pom.xml services/gateway-service/
COPY services/user-service/pom.xml services/user-service/
COPY services/inventory-service/pom.xml services/inventory-service/
COPY services/order-service/pom.xml services/order-service/
COPY services/notification-service/pom.xml services/notification-service/

# Download dependencies (cached if POMs don't change)
RUN mvn dependency:go-offline -B

# Copy all source files across all services/modules
COPY . .

# Build only the target service and its dependencies
RUN mvn clean package -pl :inventory-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/services/inventory-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]