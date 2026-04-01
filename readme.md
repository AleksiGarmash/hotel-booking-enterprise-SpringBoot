# Hotel Booking System (Enterprise Spring Boot) 

## Описание проекта

**Полноценная система бронирования отелей** с **JPA**, **Spring Security**, **Kafka**, **MongoDB** и **CSV экспортом статистики**.

**Сущности**: User → Hotel → Room → Booking + **Event-Driven Statistics** (Kafka → MongoDB).

---

## Архитектура
```text

Frontend → REST API → Services → JPA/PostgreSQL
↓ Kafka Events
MongoDB Statistics ← Kafka Consumer
```

**Технологии**:
- Spring Boot 3.x + JPA + Specifications
- Spring Security (ROLE_ADMIN/USER)
- Kafka (Producer/Consumer) + 3 топика
- MongoDB Reactive для статистики
- MapStruct + Delegate Mappers
- CSV Export (Apache Commons CSV)
- Docker Compose (PostgreSQL + Kafka + MongoDB)

---

## Безопасность

**Пользователи** (DataInit):
- admin:admin123 → ROLE_ADMIN
- user:user123 → ROLE_USER

**Права доступа**:

| Ресурс | ADMIN | USER |
|--------|-------|------|
| `/api/users/register` | permitAll | permitAll |
| `/api/hotels/**` | CRUD | GET |
| `/api/rooms/**` | CRUD | GET |
| `/api/bookings/**` | CRUD | GET (свои) |
| `/api/statistics/**` | Только | - |

---

## Эндпоинты API

### Users `/api/users`
```http
POST /api/users/register # Регистрация (permitAll)
GET /api/users # Список + пагинация (ADMIN)
GET /api/users/{id} # По ID (ADMIN)
```


### Hotels `/api/hotels`
```http
GET /api/hotels # Список + пагинация
GET /api/hotels/{id} # По ID
POST /api/hotels # Создать (ADMIN)
PUT /api/hotels/{id} # Обновить (ADMIN)
POST /api/hotels/{id}/rating # Оценить 1-5 
GET /api/hotels/search # Фильтры + пагинация
```

### Rooms `/api/rooms`
```http
GET /api/rooms # Список + пагинация
POST /api/rooms # Создать (ADMIN)
GET /api/rooms/search # Поиск по датам/цене/гостям
```

### Bookings `/api/bookings`
```http
POST /api/bookings # Создать бронь (проверка конфликтов)
PUT /api/bookings/{id} # Обновить даты/комнату
DELETE /api/bookings/{id} # Отменить
GET /api/bookings/search # Фильтры + пагинация
```

### Statistics `/api/statistics` (ADMIN)
```http
GET /api/statistics # Все события
GET /api/statistics/export/csv # CSV экспорт
GET /api/statistics/range # По датам
```

---

## Ключевые фичи

### 1. **Kafka Event-Driven**
- `User.register()` → user-registration topic → MongoDB Statistic
- `Booking.create()` → booking-events topic → MongoDB Statistic

### 2. **Smart Booking Logic**
```java
// Проверка конфликтов брони
boolean isRoomAvailable(checkIn, checkOut) {
    return bookings.noneMatch(b → b.overlaps(checkIn, checkOut))
}

// Авто-расчет цены
totalPrice = room.price × days
```

### 3. **Rating System**
```java
// Динамический рейтинг отеля
newRating = (oldRating × count + assessment) / (count + 1)
```

### 4. **Specifications**
- Hotel: город, рейтинг, расстояние до центра
- Room: цена, гости, доступность по датам

---

## Запуск

```bash
docker-compose up -d
mvn spring-boot:run
```

## Тестирование (Postman)

### 1. **Регистрация** (permitAll)
```bash
curl -X POST http://localhost:8080/api/users/register \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"username":"guest","email":"guest@mail.com"}'
```

### 2. **Создать отель** (ADMIN)
```bash
curl -X POST http://localhost:8080/api/hotels \
  -u admin:admin123 \
  -d '{"name":"Hilton","city":"Москва"}'
```

### 3. **Забронировать комнату**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -u user:user123 \
  -d '{
    "roomId": 1,
    "userId": 2, 
    "checkInDate": "2026-04-10",
    "checkOutDate": "2026-04-15"
  }'
```

### 4. **Статистика** (ADMIN)
```bash
curl http://localhost:8080/api/statistics/export/csv -u admin:admin123
# ↓ statistics.csv скачивается!
```

---