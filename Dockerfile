# Multi-stage Dockerfile for Health Management System (Backend on Render)
# Stage 1: Build the Maven application with Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy Maven descriptor and source code
COPY pom.xml .
COPY src ./src
COPY frontend ./frontend

# Package application into a standalone executable JAR
RUN mvn clean package -DskipTests

# Stage 2: Minimal Eclipse Temurin JRE 21 Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy compiled JAR and static assets
COPY --from=build /app/target/health-management-system-1.0.0.jar app.jar
COPY --from=build /app/frontend ./frontend

# Render dynamically sets PORT environment variable (typically 10000)
ENV PORT=8085
EXPOSE 8085 10000

# Run Spring Boot with dynamic port mapping
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8085} -jar app.jar"]
