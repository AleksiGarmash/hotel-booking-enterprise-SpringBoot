package com.example.spring.booking.statistic.producer;

import com.example.spring.booking.statistic.event.BookingEvent;
import com.example.spring.booking.statistic.event.UserRegistrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistration(UserRegistrationEvent event) {
        log.info("=== START publishUserRegistration ===");
        log.info("Event before: eventId={}, userId={}, timestamp={}",
                event.getEventId(), event.getUserId(), event.getTimestamp());

        try {
            event.setEventId(UUID.randomUUID().toString());
            event.setEventType("USER_REGISTRATION");
            event.setTimestamp(LocalDateTime.now());

            log.info("Event after: eventId={}, userId={}, timestamp={}",
                    event.getEventId(), event.getUserId(), event.getTimestamp());

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send("user-registration", event.getUserId().toString(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ SUCCESS: User registration event sent to Kafka, offset: {}",
                            result.getRecordMetadata().offset());
                } else {
                    log.error("❌ FAILED: Error sending to Kafka: {}", ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("❌ EXCEPTION: Failed to send Kafka message: {}", e.getMessage(), e);
        }
        log.info("=== END publishUserRegistration ===");
    }

    public void publishBookingEvent(BookingEvent event) {
        log.info("=== START publishBookingEvent ===");

        try {
            event.setEventId(UUID.randomUUID().toString());
            event.setEventType("BOOKING_CREATED");
            event.setTimestamp(LocalDateTime.now());

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send("booking-events", event.getBookingId().toString(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ SUCCESS: Booking event sent to Kafka, offset: {}",
                            result.getRecordMetadata().offset());
                } else {
                    log.error("❌ FAILED: Error sending booking event: {}", ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("❌ EXCEPTION: Failed to send booking event: {}", e.getMessage());
        }
        log.info("=== END publishBookingEvent ===");
    }
}
