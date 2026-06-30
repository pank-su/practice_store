# Practice Store

Учебный full-stack store-проект: Go REST API, PostgreSQL, JWT-авторизация и фронтенд на Kotlin/Kobweb.

```text
Бэкенд:       Go + net/http + GORM + PostgreSQL
Фронтенд:     Kotlin + Kobweb + сгенерированный Ktor-клиент
Документация: OpenAPI / Swagger
Деплой:       Docker Compose + nginx
```

## Возможности

- Регистрация и авторизация пользователей через JWT.
- Хеширование паролей через bcrypt.
- CRUD пользователей с пагинацией и фильтрацией по возрасту.
- Создание и просмотр заказов пользователя.
- OpenAPI/Swagger документация из аннотаций Go-кода.
- Генерация Kotlin Multiplatform Ktor-клиента из OpenAPI.
- Kobweb-фронтенд с разделением на `data`, `domain` и `ui` слои.
- Docker Compose для PostgreSQL, бэкенда и nginx-фронтенда.
- Модульные тесты для бэкенда.

## Архитектура

```text
.
├── cmd/main.go                 # Точка входа Go API
├── internal/                   # Бэкенд
│   ├── config/                 # Конфигурация из переменных окружения
│   ├── handlers/               # HTTP-обработчики и Swagger-аннотации
│   ├── middleware/             # JWT middleware
│   ├── models/                 # GORM-модели
│   ├── repository/             # Доступ к данным
│   ├── services/               # Бизнес-логика
│   ├── testutil/               # Тестовые утилиты
│   └── utils/                  # JWT, bcrypt, JSON-утилиты
├── migrations/                 # SQL-миграции
├── docs/                       # Сгенерированный Swagger, не хранится в git
├── app/                        # Kobweb-фронтенд
│   ├── nginx.conf              # Конфигурация nginx для статического фронтенда
│   └── site/src/jsMain/kotlin/us/panks/
│       ├── data/               # API-репозитории и ServiceLocator
│       ├── domain/             # Модели, репозитории, сценарии использования
│       ├── pages/              # Страницы Kobweb
│       └── ui/                 # ViewModels и UI-компоненты
├── Dockerfile                  # Образ бэкенда
├── Dockerfile.frontend         # nginx-образ фронтенда
├── docker-compose.yml
└── Makefile
```

## Требования

- Go `1.25+`
- PostgreSQL `16+`
- Java `17+` для Gradle/Kobweb
- Docker и Docker Compose
- `swag` CLI для генерации OpenAPI:

```bash
go install github.com/swaggo/swag/cmd/swag@latest
```

## Конфигурация

Бэкенд читает настройки из переменных окружения:

```env
HTTP_ADDR=:8080
DB_HOST=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=postgres
DB_NAME=users_orders
DB_SSLMODE=disable
JWT_SECRET=change-me-in-production
JWT_TTL_HOURS=24
```

Вместо отдельных `DB_*` переменных можно задать `DATABASE_URL`.

## Быстрый запуск

Запустить PostgreSQL:

```bash
docker compose up -d db
```

Сгенерировать Swagger и запустить бэкенд:

```bash
make swagger
make run
```

Бэкенд будет доступен на `http://localhost:8080`.

Swagger UI будет доступен на `http://localhost:8080/swagger/index.html`.

## Фронтенд локально

Сначала нужно сгенерировать Kotlin-клиент из OpenAPI:

```bash
make swagger-client
```

Затем запустить dev-сервер Kobweb:

```bash
cd app
./gradlew :site:kobwebRun
```

Порт dev-сервера задаётся в `app/site/.kobweb/conf.yaml`. Сейчас используется `8081`.

## Docker Compose

Запуск бэкенда и базы:

```bash
docker compose up --build -d db app
```

Контейнер фронтенда раздаёт уже экспортированную Kobweb-статику из `app/site/.kobweb/site`. Перед сборкой образа фронтенда нужно сделать экспорт:

```bash
cd app
./gradlew :site:kobwebExport -PkobwebReuseServer=false
cd ..
docker compose up --build -d
```

Локальные сервисы:

| Сервис | URL |
| --- | --- |
| Бэкенд API | `http://localhost:8080` |
| Фронтенд nginx | `http://localhost:8082` |
| PostgreSQL | `localhost:5432` |

## Роутинг в проде

На деплое фронтенд открывается с корня домена, а API проксируется под `/api`:

| Путь | Назначение |
| --- | --- |
| `/` | Kobweb-фронтенд |
| `/api/*` | Go API |
| `/swagger/index.html` | Swagger UI |
| `/swagger/doc.json` | OpenAPI JSON |
| `/openapi.yaml` | OpenAPI YAML |
| `/health` | Healthcheck |

Сам Go-сервис не знает про `/api`: этот префикс добавляется reverse proxy на уровне инфраструктуры.

## API

При прямом обращении к Go-сервису на `localhost:8080` используются пути без `/api`.

| Метод | Путь | Описание | Авторизация |
| --- | --- | --- | --- |
| `POST` | `/auth/login` | Авторизация и выдача JWT | нет |
| `POST` | `/users` | Регистрация пользователя | нет |
| `GET` | `/users` | Список пользователей | да |
| `GET` | `/users/{id}` | Пользователь по ID | да |
| `PUT` | `/users/{id}` | Обновление пользователя | да |
| `DELETE` | `/users/{id}` | Удаление пользователя | да |
| `POST` | `/users/{user_id}/orders` | Создание заказа | да |
| `GET` | `/users/{user_id}/orders` | Заказы пользователя | да |
| `GET` | `/health` | Проверка здоровья | нет |
| `GET` | `/swagger/index.html` | Swagger UI | нет |

## OpenAPI и Kotlin-клиент

Swagger-файлы генерируются локально и не коммитятся:

```bash
make swagger
```

Сгенерировать Swagger и Kotlin/Ktor-клиент для фронтенда:

```bash
make swagger-client
```

Сгенерированный Kotlin-клиент находится в `app/site/build/generated/openapi` и не хранится в git.

## Проверки

Тесты бэкенда:

```bash
make test
```

Проверка компиляции фронтенда:

```bash
cd app
./gradlew :site:compileKotlinJs
```

## Команды

| Команда | Что делает |
| --- | --- |
| `make tidy` | Обновляет Go-зависимости |
| `make build` | Собирает бинарник бэкенда |
| `make run` | Запускает бэкенд локально |
| `make test` | Запускает тесты бэкенда |
| `make swagger` | Генерирует OpenAPI/Swagger |
| `make swagger-client` | Генерирует Swagger и Kotlin-клиент |
| `make docker-up` | Запускает Docker Compose |
| `make docker-down` | Останавливает Docker Compose |
