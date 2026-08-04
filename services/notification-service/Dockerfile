FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY services/gateway-service/pom.xml services/gateway-service/
COPY services/user-service/pom.xml services/user-service/
COPY services/inventory-service/pom.xml services/inventory-service/
COPY services/order-service/pom.xml services/order-service/
COPY services/notification-service/pom.xml services/notification-service/
COPY services/notification-service services/notification-service
RUN mvn clean package -pl services/notification-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/services/notification-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
