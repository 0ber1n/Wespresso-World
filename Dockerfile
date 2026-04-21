# ── Stage 1: Build Frontend ───────────────────────────────────────────────────
FROM node:20-alpine AS frontend-builder
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ .
RUN npm run build

# ── Stage 2: Build Backend ────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS backend-builder
WORKDIR /app
COPY backend/.mvn/ .mvn
COPY backend/mvnw backend/pom.xml ./
RUN ./mvnw dependency:resolve
COPY backend/src ./src
RUN ./mvnw package -DskipTests

# ── Stage 3: Final Image ──────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Install nginx and supervisord
RUN apk add --no-cache nginx supervisor

# Frontend static files
COPY --from=frontend-builder /app/dist /usr/share/nginx/html

# Nginx config
COPY frontend/nginx.conf /etc/nginx/http.d/default.conf

# Backend jar
COPY --from=backend-builder /app/target/wespresso-world-0.0.1-SNAPSHOT.jar /app/backend.jar

# Supervisord config
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Frontend on 80, backend on 1337
EXPOSE 80 1337

CMD ["supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]

