package com.example.spring.booking.statistic;

import com.example.spring.booking.statistic.consumer.StatisticsKafkaConsumer;
import com.example.spring.booking.statistic.document.StatisticDocument;
import com.example.spring.booking.statistic.event.BookingEvent;
import com.example.spring.booking.statistic.event.UserRegistrationEvent;
import com.example.spring.booking.statistic.producer.KafkaEventPublisher;
import com.example.spring.booking.statistic.repository.StatisticRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaEventPublisher publisher;

    // ─── publishUserRegistration ──────────────────────────────────

    @Test
    @DisplayName("publishUserRegistration: отправляет в топик 'user-registration' с userId как ключом")
    void publishUserRegistration_sendsToCorrectTopicWithKey() {
        UserRegistrationEvent event = UserRegistrationEvent.builder()
                .userId(42L).username("aleksi").email("aleksi@mail.com").build();

        publisher.publishUserRegistration(event);

        verify(kafkaTemplate).send(
                eq("user-registration"),
                eq("42"),          // ключ = userId.toString()
                any(UserRegistrationEvent.class)
        );
    }

    @Test
    @DisplayName("publishUserRegistration: автоматически проставляет eventId, eventType и timestamp")
    void publishUserRegistration_setsEventMetadata() {
        UserRegistrationEvent event = UserRegistrationEvent.builder()
                .userId(1L).username("user").email("u@mail.com").build();

        publisher.publishUserRegistration(event);

        assertThat(event.getEventId()).isNotNull().isNotBlank();
        assertThat(event.getEventType()).isEqualTo("USER_REGISTRATION");
        assertThat(event.getTimestamp()).isNotNull();
    }

    // ─── publishBookingEvent ──────────────────────────────────────

    @Test
    @DisplayName("publishBookingEvent: отправляет в топик 'booking-events' с bookingId как ключом")
    void publishBookingEvent_sendsToCorrectTopicWithKey() {
        BookingEvent event = BookingEvent.builder()
                .bookingId(10L).userId(2L).roomId(1L).hotelId(1L)
                .checkInDate(LocalDate.of(2027, 6, 1))
                .checkOutDate(LocalDate.of(2027, 6, 5))
                .totalPrice(8000.0).build();

        publisher.publishBookingEvent(event);

        verify(kafkaTemplate).send(
                eq("booking-events"),
                eq("10"),          // ключ = bookingId.toString()
                any(BookingEvent.class)
        );
    }

    @Test
    @DisplayName("publishBookingEvent: автоматически проставляет eventId, eventType и timestamp")
    void publishBookingEvent_setsEventMetadata() {
        BookingEvent event = BookingEvent.builder()
                .bookingId(10L).userId(2L).roomId(1L).hotelId(1L)
                .checkInDate(LocalDate.of(2027, 6, 1))
                .checkOutDate(LocalDate.of(2027, 6, 5))
                .totalPrice(8000.0).build();

        publisher.publishBookingEvent(event);

        assertThat(event.getEventId()).isNotNull().isNotBlank();
        assertThat(event.getEventType()).isEqualTo("BOOKING_CREATED");
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("publishBookingEvent: каждый вызов генерирует уникальный eventId")
    void publishBookingEvent_uniqueEventIdEachCall() {
        BookingEvent event1 = BookingEvent.builder().bookingId(1L).userId(1L)
                .roomId(1L).hotelId(1L).totalPrice(1000.0)
                .checkInDate(LocalDate.now()).checkOutDate(LocalDate.now().plusDays(1)).build();
        BookingEvent event2 = BookingEvent.builder().bookingId(2L).userId(1L)
                .roomId(1L).hotelId(1L).totalPrice(2000.0)
                .checkInDate(LocalDate.now()).checkOutDate(LocalDate.now().plusDays(2)).build();

        publisher.publishBookingEvent(event1);
        publisher.publishBookingEvent(event2);

        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }
}

@ExtendWith(MockitoExtension.class)
class StatisticsKafkaConsumerTest {

    @Mock private MongoTemplate mongoTemplate;
    @Mock private StatisticRepository statisticRepository;

    @InjectMocks
    private StatisticsKafkaConsumer consumer;

    // ─── consumerUserRegistration ─────────────────────────────────

    @Test
    @DisplayName("consumerUserRegistration: сохраняет документ с правильными полями в MongoDB")
    void consumerUserRegistration_savesDocumentWithCorrectFields() {
        UserRegistrationEvent event = UserRegistrationEvent.builder()
                .eventId("evt-123").eventType("USER_REGISTRATION")
                .userId(5L).username("aleksi").email("aleksi@mail.com")
                .timestamp(LocalDateTime.now()).build();

        when(mongoTemplate.collectionExists("statistics")).thenReturn(true);
        when(statisticRepository.save(any())).thenAnswer(inv -> {
            StatisticDocument doc = inv.getArgument(0);
            doc = StatisticDocument.builder()
                    .id("mongo-id-1")
                    .eventId(doc.getEventId())
                    .eventType(doc.getEventType())
                    .userId(doc.getUserId())
                    .username(doc.getUsername())
                    .email(doc.getEmail())
                    .build();
            return doc;
        });

        consumer.consumerUserRegistration(event);

        ArgumentCaptor<StatisticDocument> captor = ArgumentCaptor.forClass(StatisticDocument.class);
        verify(statisticRepository).save(captor.capture());

        StatisticDocument saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo("evt-123");
        assertThat(saved.getEventType()).isEqualTo("USER_REGISTRATION");
        assertThat(saved.getUserId()).isEqualTo(5L);
        assertThat(saved.getUsername()).isEqualTo("aleksi");
        assertThat(saved.getEmail()).isEqualTo("aleksi@mail.com");
    }

    @Test
    @DisplayName("consumerUserRegistration: коллекция не существует → создаётся")
    void consumerUserRegistration_collectionMissing_createsIt() {
        UserRegistrationEvent event = UserRegistrationEvent.builder()
                .eventId("e1").eventType("USER_REGISTRATION")
                .userId(1L).username("u").email("u@m.com")
                .timestamp(LocalDateTime.now()).build();

        when(mongoTemplate.collectionExists("statistics")).thenReturn(false);
        when(statisticRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consumerUserRegistration(event);

        verify(mongoTemplate).createCollection("statistics");
    }

    // ─── consumerBookingEvent ─────────────────────────────────────

    @Test
    @DisplayName("consumerBookingEvent: сохраняет документ с booking-полями")
    void consumerBookingEvent_savesDocumentWithBookingFields() {
        BookingEvent event = BookingEvent.builder()
                .eventId("book-evt-1").eventType("BOOKING_CREATED")
                .bookingId(10L).userId(2L).roomId(1L).hotelId(1L)
                .checkInDate(LocalDate.of(2027, 6, 1))
                .checkOutDate(LocalDate.of(2027, 6, 5))
                .totalPrice(8000.0).timestamp(LocalDateTime.now()).build();

        when(mongoTemplate.collectionExists("statistics")).thenReturn(true);
        when(statisticRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consumerBookingEvent(event);

        ArgumentCaptor<StatisticDocument> captor = ArgumentCaptor.forClass(StatisticDocument.class);
        verify(statisticRepository).save(captor.capture());

        StatisticDocument saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo("book-evt-1");
        assertThat(saved.getEventType()).isEqualTo("BOOKING_CREATED");
        assertThat(saved.getBookingId()).isEqualTo(10L);
        assertThat(saved.getRoomId()).isEqualTo(1L);
        assertThat(saved.getHotelId()).isEqualTo(1L);
        assertThat(saved.getTotalPrice()).isEqualTo(8000.0);
        assertThat(saved.getCheckInDate()).isEqualTo(LocalDate.of(2027, 6, 1));
        assertThat(saved.getCheckOutDate()).isEqualTo(LocalDate.of(2027, 6, 5));
    }

    @Test
    @DisplayName("consumerBookingEvent: коллекция не существует → создаётся")
    void consumerBookingEvent_collectionMissing_createsIt() {
        BookingEvent event = BookingEvent.builder()
                .eventId("e").eventType("BOOKING_CREATED")
                .bookingId(1L).userId(1L).roomId(1L).hotelId(1L)
                .totalPrice(1000.0).timestamp(LocalDateTime.now())
                .checkInDate(LocalDate.now()).checkOutDate(LocalDate.now().plusDays(1)).build();

        when(mongoTemplate.collectionExists("statistics")).thenReturn(false);
        when(statisticRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consumerBookingEvent(event);

        verify(mongoTemplate).createCollection("statistics");
    }
}