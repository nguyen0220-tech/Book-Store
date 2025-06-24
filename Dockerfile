FROM ubuntu:latest
LABEL authors="nvtuy"

ENTRYPOINT ["top", "-b"]

# Dùng JDK 22 (từ Eclipse Temurin - ổn định và phổ biến)
FROM eclipse-temurin:22

# Tạo thư mục làm việc
WORKDIR /app

# Copy file jar vào container
COPY target/secure-user-app-2.0.jar app.jar

# Khởi chạy ứng dụng
CMD ["java", "-jar", "app.jar"]
