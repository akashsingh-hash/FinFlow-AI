# Stage 1: Build the Java Spring Boot application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy files
COPY backend/pom.xml backend/
COPY backend/src backend/src

# Package the application
RUN mvn -f backend/pom.xml clean package -DskipTests

# Stage 2: Create the runtime environment
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy build artifact
COPY --from=build /app/backend/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Execute Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
