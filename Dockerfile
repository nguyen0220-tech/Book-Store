# Stage 1: Build với Maven + JDK 21
FROM maven:3.9.10-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Chạy app với JDK 21 nhẹ
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/secure-user-app-2.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
