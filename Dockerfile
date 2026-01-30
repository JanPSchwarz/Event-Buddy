# --- Stage 1: Build frontend ---
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend

# Install deps first for layer caching
COPY frontend/package*.json ./
RUN npm ci

# Copy source and build
COPY frontend/ .
RUN npm run build

# --- Stage 2: Build backend (with frontend assets) ---
FROM maven:3.9-eclipse-temurin-21 AS backend-builder
WORKDIR /app

# Copy POM to warm cache
COPY backend/pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy backend sources
COPY backend/ .

# Copy built frontend into backend static resources
COPY --from=frontend-builder /app/frontend/dist src/main/resources/static

# Build Spring Boot jar (tests optional)
RUN mvn clean package -DskipTests

# --- Stage 3: Runtime image ---
FROM amazoncorretto:21
WORKDIR /app

# Copy the jar built in previous stage (update jar name if different)
COPY --from=backend-builder /app/target/Event-Buddy.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]