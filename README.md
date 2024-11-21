# Task Management System
Проект представляет собой простую систему управления задачами (Task Management System) с использованием Java и Spring Boot. Система предоставляет API для создания, редактирования, удаления и просмотра задач, а также управления статусом, приоритетом, авторами и исполнителями задач.

## Запуск проекта:
1. Перейдите в директорию проекта:
   ```
   cd task-management-system
   ```
2. Упакуйте проект с помощью Maven:
   ```
   mvn clean package
   ```
3. Запустите проект с помощью Docker Compose:
   ```
   docker-compose up -d --build
   ```
4. API будет доступно по адресу:
    ```
    http://localhost:8080
    ```
5. Для доступа к Swagger UI перейдите по ссылке: 
    ```
    http://localhost:8080/swagger-ui/index.html
    ```