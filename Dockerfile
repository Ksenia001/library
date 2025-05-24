# file: ./Dockerfile (в корне проекта)

# Этап 1: Сборка приложения с использованием Maven
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Скачиваем зависимости (может быть закомментировано, если mvnw package справляется)
# RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw package -DskipTests

# Этап 2: Создание легковесного образа с JRE для запуска приложения
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ARG JAR_FILE_PATH=target/*.jar
COPY --from=builder /app/${JAR_FILE_PATH} application.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]