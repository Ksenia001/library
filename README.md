# Library REST Service

Это проект для лабораторной работы, в котором реализован простой REST-сервис библиотеки с использованием Java, Spring Boot и Maven.

## Описание
Проект предназначен для получения списка книг, фильтрации по автору и получения деталей книги по её идентификатору через GET-эндпоинты.

## Требования
- Java 17
- Spring Boot
- Maven

## Как запустить проект
1. Склонируйте репозиторий: git clone https://github.com/Ksenia001/Library
2. Перейдите в папку проекта:
   cd library
3. Запустите проект: mvn spring-boot:run
4. Проверьте работу эндпоинтов в браузере или через Postman:
- Получение списка книг: `http://localhost:8080/library/books`
- Получение книги по ID: `http://localhost:8080/library/books/1`

## Sonar
https://sonarcloud.io/project/overview?id=Ksenia001_library

