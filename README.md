# Users and Orders API

REST API на Go для управления пользователями и их заказами с JWT-авторизацией, PostgreSQL и GORM.

## Возможности

- CRUD пользователей с пагинацией и фильтрацией по возрасту
- Управление заказами пользователей
- JWT-авторизация (bcrypt для хеширования паролей)
- Автогенерация OpenAPI/Swagger документации из аннотаций кода
- Логирование основных операций
- Docker Compose для запуска
- Unit-тесты

## Структура проекта

```
├── cmd/main.go              # Точка входа
├── internal/
│   ├── config/              # Конфигурация из env
│   ├── handlers/            # HTTP-обработчики (+ swag-аннотации)
│   ├── middleware/          # JWT middleware
│   ├── models/              # Модели БД
│   ├── repository/          # Слой доступа к данным (GORM)
│   ├── services/            # Бизнес-логика
│   ├── testutil/            # Mock-хранилище для тестов
│   └── utils/               # JWT, bcrypt, JSON-хелперы
├── migrations/              # SQL-миграции
├── docs/                    # Сгенерированная OpenAPI-документация
├── Dockerfile
├── docker-compose.yml
├── Makefile
└── .env
```

## Запуск через Docker Compose

```bash
docker compose up --build -d
```

API будет доступен на `http://localhost:8080`, Swagger UI — на `http://localhost:8080/swagger/`.

## Локальный запуск (требуется Go 1.22+ и PostgreSQL)

1. Установить зависимости и сгенерировать документацию:

```bash
make tidy
make swagger
```

2. Запустить PostgreSQL (локально или через Docker):

```bash
docker compose up -d db
```

3. Запустить приложение:

```bash
make run
```

## Эндпоинты

| Метод | Путь | Описание | Auth |
|-------|------|----------|------|
| POST | `/auth/login` | Авторизация, выдача JWT | нет |
| POST | `/users` | Создание пользователя (регистрация) | нет |
| GET | `/users` | Список пользователей (пагинация, фильтр) | да |
| GET | `/users/{id}` | Получение пользователя по ID | да |
| PUT | `/users/{id}` | Обновление пользователя | да |
| DELETE | `/users/{id}` | Удаление пользователя | да |
| POST | `/users/{user_id}/orders` | Создание заказа | да |
| GET | `/users/{user_id}/orders` | Список заказов пользователя | да |
| GET | `/health` | Проверка здоровья | нет |
| GET | `/swagger/` | Swagger UI | нет |

## Тестирование

```bash
make test
```

## Автогенерация OpenAPI

Документация генерируется из аннотаций в коде с помощью [swaggo/swag](https://github.com/swaggo/swag):

```bash
go install github.com/swaggo/swag/cmd/swag@latest
make swagger
```

Результат: `docs/swagger.json`, `docs/swagger.yaml`.
