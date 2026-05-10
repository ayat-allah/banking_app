FROM maven:3.8.4-openjdk-17-slim AS build

WORKDIR /app

# Copy parent pom
COPY pom.xml .

# Copy all modules
COPY api-gateway api-gateway/
COPY eureka-server eureka-server/
COPY auth-service auth-service/
COPY admin-service admin-service/
COPY payment-service payment-service/
COPY transaction-service transaction-service/
COPY frontend-service frontend-service/

# Build all modules
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy just the api-gateway jar
COPY --from=build /app/api-gateway/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]