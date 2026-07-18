# syntax=docker/dockerfile:1

# ============================================================================
# Stage 1 — Build / Giai đoạn build
# Compile the app and package the runnable jar using the Maven wrapper.
# Biên dịch ứng dụng và đóng gói jar chạy được bằng Maven wrapper.
# ============================================================================
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

# Copy only the wrapper + pom first so Docker can cache the dependency layer.
# Sao chép wrapper + pom trước để Docker cache lớp tải phụ thuộc.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Now copy the source and build (skip tests for a faster image build).
# Sao chép mã nguồn rồi build (bỏ qua test để build image nhanh hơn).
COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

# Extract the jar name into a fixed path so the runtime stage is stable.
# Chuẩn hoá tên jar về một đường dẫn cố định cho stage runtime.
RUN cp target/*.jar app.jar

# ============================================================================
# Stage 2 — Runtime / Giai đoạn chạy
# Slim JRE image, runs as a non-root user.
# Image JRE gọn nhẹ, chạy bằng người dùng không phải root.
# ============================================================================
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# curl is needed for the container HEALTHCHECK below.
# curl phục vụ cho HEALTHCHECK phía dưới.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Create an unprivileged user / Tạo người dùng không đặc quyền
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /workspace/app.jar app.jar
# Uploaded files live here (see app.storage.root); a named volume is normally
# mounted over this path, but the directory must pre-exist and be writable by
# the non-root "spring" user for the case where it isn't (e.g. local `docker run`).
RUN mkdir -p /app/data && chown -R spring:spring /app
USER spring

# Data written under /app/data must survive container recreation — keep it out
# of the writable container layer.
VOLUME /app/data

EXPOSE 8080

# JVM tuned for containers (respects cgroup memory limits).
# JVM tối ưu cho container (tôn trọng giới hạn bộ nhớ cgroup).
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENV APP_STORAGE_ROOT=/app/data

# Liveness/readiness probe via Spring Boot Actuator.
# Kiểm tra sức khoẻ qua Spring Boot Actuator. start-period=90s: cold first boot
# runs Flyway V1->V5 including a large seed (700+ INSERTs) before Actuator is UP.
HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
