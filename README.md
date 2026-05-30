# FilmApp Backend Server

FilmApp Backend — серверная часть клиент–серверного приложения-справочника по фильмам.

Backend реализует REST API для Android-клиента и отвечает за:

- авторизацию пользователей;
- генерацию и проверку JWT-токенов;
- работу с каталогом фильмов;
- управление избранным;
- управление списком «Посмотреть позже»;
- работу с жанрами;
- хранение данных в PostgreSQL.

Сервер разработан на Kotlin с использованием Ktor и JDBC.

---

# 1. Основные возможности

## Авторизация
- Регистрация пользователей
- Логин пользователей
- Генерация JWT-токена
- Разделение ролей (`USER`, `ADMIN`)

## Фильмы
- Получение списка фильмов
- Поиск фильмов
- Фильтрация по жанрам
- Получение фильма по ID
- Получение случайного фильма
- Добавление фильма (ADMIN)
- Обновление фильма (ADMIN)
- Удаление фильма (ADMIN)

## Избранное
- Добавление фильма в избранное
- Удаление из избранного
- Получение списка избранного

## Watch Later
- Добавление фильма в список
- Удаление фильма из списка
- Получение списка

## Жанры
- Получение списка жанров

---

# 2. Technology Stack

## Backend
- Kotlin
- Ktor (REST API)
- JWT Authentication
- Kotlinx Serialization

## Database
- PostgreSQL

## Data Access
- JDBC
- DataSource
- PreparedStatement / ResultSet

## Connection Pool
- HikariCP

## Infrastructure
- Docker (PostgreSQL container)

---

# 3. Архитектура

Сервер построен по слоистой архитектуре:

Routes → Repositories → Database → Response DTO

Пример потока:
```HTTP Request
↓
FilmRoutes.kt
↓
FilmRepository
↓
SQL Query (JDBC)
↓
PostgreSQL
↓
DTO Response
↓
JSON Response
```

# 4. Структура проекта
```
com.filmapp
│
├── Application.kt
│
├── dto
│ ├── AuthDto.kt
│ └── FilmDto.kt
│
├── models
│ ├── Film.kt
│ ├── User.kt
│ └── UserRole.kt
│
├── plugins
│ ├── Database.kt
│ ├── Routing.kt
│ ├── Security.kt
│ └── Serialization.kt
│
├── repositories
│ ├── FilmRepository.kt
│ ├── GenreRepository.kt
│ └── UserRepository.kt
│
├── routes
│ ├── AuthRoutes.kt
│ ├── FilmRoutes.kt
│ ├── GenreRoutes.kt
│ └── WatchLaterRoutes.kt
│
└── security
└── RoleUtils.kt
```

---

# 5. REST API

## Base URL: http://localhost:8081/api/v1/


---

## Auth

### POST /auth/register

Регистрация пользователя

```json
{
  "email": "user@mail.com",
  "password": "123456",
  "name": "Alex"
}
```
### POST /auth/login

Авторизация пользователя

{
  "email": "user@mail.com",
  "password": "123456"
}

## Films
### GET /films

Параметры:

page
pageSize
search
genreId

### GET /films/{id}

Получение фильма по ID

### GET /films/random

Получение случайного фильма

## Установка и запуск 
Требования
JDK 17+
Docker
PostgreSQL
Gradle

Запуск базы данных
```
docker compose up -d
```

Запуск сервера
```
./gradlew run
```

Сервер будет доступен по адресу:
```
http://localhost:8081
```