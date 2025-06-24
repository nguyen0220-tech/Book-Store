FROM ubuntu:latest
LABEL authors="nvtuy"

ENTRYPOINT ["top", "-b"]

# Stage 1: Build ứng dụng với Maven và JDK 22
FROM maven:3.9.4-eclipse-temurin-22 AS build

WORKDIR /app

# Copy cấu hình Maven và source code
COPY pom.xml .
COPY src ./src

# Build project, bỏ qua test để nhanh
RUN mvn clean package -DskipTests

# Stage 2: Runtime với JDK nhẹ hơn
FROM eclipse-temurin:22-jdk-jammy

WORKDIR /app

# Copy file jar đã build từ stage 1
COPY --from=build /app/target/secure-user-app-2.0.jar app.jar

# Lệnh chạy ứng dụng
CMD ["java", "-jar", "app.jar"]
