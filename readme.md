# Hotel Booking System

Event-driven система бронирования отелей на Spring Boot.

## Стек технологий

| Слой | Технологии |
|------|-----------|
| Backend | Java 17, Spring Boot 3.1.5 |
| Security | Spring Security + JWT |
| База данных | PostgreSQL, MongoDB (Event Sourcing) |
| Messaging | Apache Kafka (3 топика) |
| Прочее | MapStruct, Lombok, Docker Compose |

## Архитектура

```
src/main/java/com/example/spring/booking/
├── config/          — SecurityConfig, SitesList
├── controllers/     — BookingController, HotelController, RoomController, UserController
├── entity/          — Booking, Hotel, Room, User, Role
├── exception/       — GlobalExceptionHandler, ResourceNotFoundException, RoomNotAvailableException
├── mapper/          — BookingMapper, HotelMapper (MapStruct + Delegate)
├── repository/      — BookingRepository, HotelRepository, RoomRepository, UserRepository
├── services/        — бизнес-логика (BookingService, HotelService, UserService)
├── statistic/
│   ├── producer/    — KafkaEventPublisher
│   ├── consumer/    — StatisticsKafkaConsumer
│   ├── event/       — BookingEvent, UserRegistrationEvent
│   └── document/    — StatisticDocument (MongoDB)
└── web/model/       — DTO запросов и ответов
```

## Как запустить

### 1. Требования
- Java 17+, Maven 3.8+
- PostgreSQL 15+
- MongoDB
- Docker Compose (для Kafka)

### 2. Запустить инфраструктуру

```bash
docker-compose up -d
```

Docker Compose поднимает Kafka + Zookeeper.

### 3. Создать базы данных

```sql
-- PostgreSQL
CREATE DATABASE hotel_booking;
```

MongoDB создаётся автоматически.

### 4. Настроить `application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hotel_booking
    username: postgres
    password: ваш_пароль
  data:
    mongodb:
      uri: mongodb://localhost:27017/hotel_statistics

spring:
  kafka:
    bootstrap-servers: localhost:9092
```

### 5. Запустить

```bash
mvn spring-boot:run
```

## API

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| POST | `/api/users` | Регистрация |
| GET | `/api/hotels` | Список отелей |
| POST | `/api/hotels/{id}/rating` | Оценить отель (1–5) |
| GET | `/api/rooms?hotelId=&checkIn=&checkOut=` | Доступные номера |
| POST | `/api/bookings` | Создать бронь |
| DELETE | `/api/bookings/{id}` | Отменить бронь |
| GET | `/api/statistics/export` | Экспорт статистики (CSV) |

## Kafka

Три топика обрабатываются независимо:
- `booking-events` — создание брони → Consumer сохраняет событие в MongoDB
- `user-registration` — регистрация → статистика пользователей

## Запуск тестов

```bash
mvn test
```

Покрытие: BookingServiceTest (10 кейсов), HotelServiceTest (12 кейсов), KafkaTests, UserServiceTest.
