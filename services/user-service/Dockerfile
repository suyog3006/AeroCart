FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy full repository context (required for root pom.xml & sub-modules)
COPY . .

# Build only user-service and its dependencies
RUN mvn -B -pl :user-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the generated executable JAR explicitly from the user-service target folder
COPY --from=builder /app/services/user-service/target/user-service-*.jar app.jar

# Expose the correct port for user-service (Update this if your user-service uses a different port!)
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]