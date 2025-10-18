# Используем официальный образ OpenJDK с Alpine Linux (легковесный)
FROM eclipse-temurin:17-jdk-alpine

# Создаем директорию для приложения
WORKDIR /app

# Копируем JAR-файл в контейнер
COPY CarButtle-1.0-SNAPSHOT.jar app.jar

# Открываем порт, который использует приложение (обычно 8080 для Spring Boot)
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]