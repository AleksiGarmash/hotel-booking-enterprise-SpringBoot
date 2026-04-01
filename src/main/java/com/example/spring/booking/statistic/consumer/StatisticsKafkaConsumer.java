package com.example.spring.booking.statistic.consumer;

import com.example.spring.booking.statistic.document.StatisticDocument;
import com.example.spring.booking.statistic.event.BookingEvent;
import com.example.spring.booking.statistic.event.UserRegistrationEvent;
import com.example.spring.booking.statistic.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticsKafkaConsumer {

    private final MongoTemplate mongoTemplate;
    private final StatisticRepository statisticRepository;

    @KafkaListener(topics = "user-registration", groupId = "hotel-statistics-group")
    public void consumerUserRegistration(UserRegistrationEvent event) {
        log.info("=== CONSUMER START: UserRegistrationEvent ===");
        log.info("Received event: eventId={}, userId={}, username={}, timestamp={}",
                event.getEventId(), event.getUserId(), event.getUsername(), event.getTimestamp());

        try {
            // Проверяем коллекцию
            boolean collectionExists = mongoTemplate.collectionExists("statistics");
            log.info("Collection 'statistics' exists: {}", collectionExists);

            if (!collectionExists) {
                log.info("Creating collection 'statistics'");
                mongoTemplate.createCollection("statistics");
                log.info("Collection created");
            }

            // Создаем документ
            StatisticDocument document = StatisticDocument.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .timestamp(event.getTimestamp())
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .email(event.getEmail())
                    .build();

            log.info("Document to save: {}", document);

            // Сохраняем
            StatisticDocument saved = statisticRepository.save(document);
            log.info("✅ DOCUMENT SAVED in MongoDB with ID: {}", saved.getId());

        } catch (Exception e) {
            log.error("❌ ERROR saving to MongoDB: {}", e.getMessage(), e);
        }
        log.info("=== CONSUMER END ===");
    }

    @KafkaListener(topics = "booking-events", groupId = "hotel-statistics-group")
    public void consumerBookingEvent(BookingEvent event) {
        log.info("=== CONSUMER START: BookingEvent ===");
        log.info("Received event: eventId={}, bookingId={}, userId={}",
                event.getEventId(), event.getBookingId(), event.getUserId());

        try {
            if (!mongoTemplate.collectionExists("statistics")) {
                log.info("Creating collection 'statistics'");
                mongoTemplate.createCollection("statistics");
            }

            StatisticDocument document = StatisticDocument.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .timestamp(event.getTimestamp())
                    .bookingId(event.getBookingId())
                    .userId(event.getUserId())
                    .roomId(event.getRoomId())
                    .hotelId(event.getHotelId())
                    .checkInDate(event.getCheckInDate())
                    .checkOutDate(event.getCheckOutDate())
                    .totalPrice(event.getTotalPrice())
                    .build();

            StatisticDocument saved = statisticRepository.save(document);
            log.info("✅ BOOKING DOCUMENT SAVED with ID: {}", saved.getId());

        } catch (Exception e) {
            log.error("❌ ERROR saving booking to MongoDB: {}", e.getMessage(), e);
        }
        log.info("=== CONSUMER END ===");
    }
}
