FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy full repository context (required for root pom.xml & sub-modules)
COPY . .

# Build only notification-service and its dependencies
RUN mvn -B -pl services/notification-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the generated executable JAR explicitly
COPY --from=builder /app/services/notification-service/target/notification-service-*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]