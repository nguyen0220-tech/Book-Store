# Stage 1: Build with Maven and JDK 22
FROM maven:3.9.4-eclipse-temurin-22-jammy AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run with JDK 22 slim
FROM eclipse-temurin:22-jre-jammy

WORKDIR /app
COPY --from=build /app/target/secure-user-app-2.0.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
