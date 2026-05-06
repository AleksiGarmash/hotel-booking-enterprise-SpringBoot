package com.example.spring.booking.service;

import com.example.spring.booking.entity.*;
import com.example.spring.booking.exception.ResourceNotFoundException;
import com.example.spring.booking.exception.RoomNotAvailableException;
import com.example.spring.booking.mapper.BookingMapper;
import com.example.spring.booking.repository.BookingRepository;
import com.example.spring.booking.repository.RoomRepository;
import com.example.spring.booking.repository.UserRepository;
import com.example.spring.booking.statistic.producer.KafkaEventPublisher;
import com.example.spring.booking.web.model.booking.BookingRequest;
import com.example.spring.booking.web.model.booking.BookingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private KafkaEventPublisher kafkaEventPublisher;

    @InjectMocks
    private BookingService bookingService;

    private Hotel hotel;
    private Room room;
    private User user;
    private BookingRequest request;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder().id(1L).name("Hilton").title("Hilton Hotel")
                .city("Москва").address("ул. Тверская, 1").distanceToCenter(0.5).build();

        room = Room.builder().id(1L).name("Standard").number("101")
                .price(2000.0).maxGuests(2).hotel(hotel).build();

        user = User.builder().id(2L).username("testuser")
                .password("pass").email("test@mail.com").role(Role.ROLE_USER).build();

        request = new BookingRequest();
        request.setRoomId(1L);
        request.setUserId(2L);
        request.setCheckInDate(LocalDate.of(2027, 6, 1));
        request.setCheckOutDate(LocalDate.of(2027, 6, 5));
    }

    // Успешное создание

    @Test
    @DisplayName("create: комната свободна → бронь сохраняется и Kafka-событие публикуется")
    void create_roomAvailable_savesAndPublishesEvent() {
        Booking savedBooking = Booking.builder()
                .id(10L).room(room).user(user)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate()).build();

        BookingResponse response = new BookingResponse();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(bookingRepository.findConflictBooking(any(), any(), any())).thenReturn(List.of());
        when(bookingMapper.requestToBooking(request)).thenReturn(savedBooking);
        when(bookingRepository.save(any())).thenReturn(savedBooking);
        when(bookingMapper.bookingToResponse(savedBooking)).thenReturn(response);

        BookingResponse result = bookingService.create(request);

        assertThat(result).isNotNull();
        verify(bookingRepository).save(any(Booking.class));
        verify(kafkaEventPublisher).publishBookingEvent(any());
    }

    // Конфликт дат

    @Test
    @DisplayName("create: есть конфликтная бронь → RoomNotAvailableException")
    void create_conflictExists_throwsRoomNotAvailableException() {
        Booking conflict = Booking.builder()
                .checkInDate(LocalDate.of(2027, 6, 3))
                .checkOutDate(LocalDate.of(2027, 6, 7))
                .room(room).user(user).build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(bookingRepository.findConflictBooking(eq(1L),
                eq(request.getCheckInDate()), eq(request.getCheckOutDate())))
                .thenReturn(List.of(conflict));

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(RoomNotAvailableException.class)
                .hasMessageContaining("unavailable");
    }

    // Валидация дат

    @Test
    @DisplayName("create: checkIn позже checkOut → IllegalArgumentException")
    void create_checkInAfterCheckOut_throwsIllegalArgument() {
        request.setCheckInDate(LocalDate.of(2027, 6, 10));
        request.setCheckOutDate(LocalDate.of(2027, 6, 5));

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Check-in date can't be later");
    }

    @Test
    @DisplayName("create: checkIn == checkOut → IllegalArgumentException (минимум 1 день)")
    void create_sameDay_throwsIllegalArgument() {
        LocalDate sameDay = LocalDate.of(2027, 6, 5);
        request.setCheckInDate(sameDay);
        request.setCheckOutDate(sameDay);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minimum for 1 day");
    }

    @Test
    @DisplayName("create: checkIn == null → IllegalArgumentException")
    void create_nullCheckIn_throwsIllegalArgument() {
        request.setCheckInDate(null);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // Ресурсы не найдены

    @Test
    @DisplayName("create: комната не найдена → ResourceNotFoundException")
    void create_roomNotFound_throwsResourceNotFoundException() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(bookingRepository, kafkaEventPublisher);
    }

    @Test
    @DisplayName("create: пользователь не найден → ResourceNotFoundException")
    void create_userNotFound_throwsResourceNotFoundException() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(bookingRepository, kafkaEventPublisher);
    }

    // Поиск

    @Test
    @DisplayName("findById: существующий ID → возвращает ответ")
    void findById_exists_returnsResponse() {
        Booking booking = Booking.builder().id(10L).room(room).user(user).build();
        BookingResponse response = new BookingResponse();

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingMapper.bookingToResponse(booking)).thenReturn(response);

        BookingResponse result = bookingService.findById(10L);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("findById: несуществующий ID → ResourceNotFoundException")
    void findById_notFound_throwsResourceNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // Отмена

    @Test
    @DisplayName("cancel: существующий ID → deleteById вызван")
    void cancel_exists_deleteCalled() {
        when(bookingRepository.existsById(10L)).thenReturn(true);

        bookingService.cancel(10L);

        verify(bookingRepository).deleteById(10L);
    }

    @Test
    @DisplayName("cancel: несуществующий ID → ResourceNotFoundException, deleteById не вызван")
    void cancel_notFound_throwsAndNoDeletion() {
        when(bookingRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bookingService.cancel(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookingRepository, never()).deleteById(any());
    }

    // Kafka: событие не отправляется при ошибке

    @Test
    @DisplayName("create: при конфликте Kafka-событие НЕ публикуется")
    void create_conflict_kafkaNotCalled() {
        Booking conflict = Booking.builder()
                .checkInDate(LocalDate.of(2027, 5, 30))
                .checkOutDate(LocalDate.of(2027, 6, 3))
                .room(room).user(user).build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(bookingRepository.findConflictBooking(any(), any(), any()))
                .thenReturn(List.of(conflict));

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(RoomNotAvailableException.class);

        verifyNoInteractions(kafkaEventPublisher);
    }
}