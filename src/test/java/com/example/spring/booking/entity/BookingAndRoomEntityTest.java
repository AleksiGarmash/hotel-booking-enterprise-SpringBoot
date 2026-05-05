package com.example.spring.booking.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BookingEntityTest {

    private static final LocalDate JUN_01 = LocalDate.of(2027, 6, 1);
    private static final LocalDate JUN_05 = LocalDate.of(2027, 6, 5);
    private static final LocalDate JUN_10 = LocalDate.of(2027, 6, 10);
    private static final LocalDate JUN_15 = LocalDate.of(2027, 6, 15);

    private Booking existingBooking;

    @BeforeEach
    void setUp() {
        existingBooking = Booking.builder()
                .checkInDate(JUN_05)
                .checkOutDate(JUN_10)
                .build();
    }

    @Test
    @DisplayName("overlaps: новая бронь полностью внутри существующей → true")
    void overlaps_newInsideExisting_returnsTrue() {
        // существующая: [05 - 10], новая: [06 - 08] → конфликт
        assertThat(existingBooking.overlaps(
                LocalDate.of(2027, 6, 6),
                LocalDate.of(2027, 6, 8)
        )).isTrue();
    }

    @Test
    @DisplayName("overlaps: новая бронь полностью перекрывает существующую → true")
    void overlaps_newCoversExisting_returnsTrue() {
        // существующая: [05 - 10], новая: [01 - 15] → конфликт
        assertThat(existingBooking.overlaps(JUN_01, JUN_15)).isTrue();
    }

    @Test
    @DisplayName("overlaps: частичное перекрытие начала → true")
    void overlaps_partialOverlapAtStart_returnsTrue() {
        // существующая: [05 - 10], новая: [03 - 07] → конфликт
        assertThat(existingBooking.overlaps(
                LocalDate.of(2027, 6, 3),
                LocalDate.of(2027, 6, 7)
        )).isTrue();
    }

    @Test
    @DisplayName("overlaps: частичное перекрытие конца → true")
    void overlaps_partialOverlapAtEnd_returnsTrue() {
        // существующая: [05 - 10], новая: [08 - 13] → конфликт
        assertThat(existingBooking.overlaps(
                LocalDate.of(2027, 6, 8),
                LocalDate.of(2027, 6, 13)
        )).isTrue();
    }

    @Test
    @DisplayName("overlaps: новая бронь до существующей (без касания) → false")
    void overlaps_newBeforeExisting_returnsFalse() {
        // существующая: [05 - 10], новая: [01 - 03] → нет конфликта
        assertThat(existingBooking.overlaps(JUN_01, LocalDate.of(2027, 6, 3))).isFalse();
    }

    @Test
    @DisplayName("overlaps: новая бронь после существующей (без касания) → false")
    void overlaps_newAfterExisting_returnsFalse() {
        // существующая: [05 - 10], новая: [12 - 15] → нет конфликта
        assertThat(existingBooking.overlaps(
                LocalDate.of(2027, 6, 12), JUN_15
        )).isFalse();
    }

    @Test
    @DisplayName("overlaps: новая бронь начинается в день выезда существующей → true (реализация включает границы)")
    void overlaps_newStartsWhenExistingEnds_returnsTrue() {
        // существующая: [05 - 10], новая: [10 - 15]
        // overlaps() использует isBefore/isAfter без OrEqual → границы считаются конфликтом
        // !(JUN_10.isBefore(JUN_10) || JUN_05.isAfter(JUN_15)) = !(false || false) = true
        assertThat(existingBooking.overlaps(JUN_10, JUN_15)).isTrue();
    }

    @Test
    @DisplayName("overlaps: новая бронь заканчивается в день заезда существующей → true (реализация включает границы)")
    void overlaps_newEndsWhenExistingStarts_returnsTrue() {
        // существующая: [05 - 10], новая: [01 - 05]
        // !(JUN_10.isBefore(JUN_01) || JUN_05.isAfter(JUN_05)) = !(false || false) = true
        assertThat(existingBooking.overlaps(JUN_01, JUN_05)).isTrue();
    }
}

class RoomEntityTest {

    private Hotel hotel;
    private Room room;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder().id(1L).name("Test").title("Test Hotel")
                .city("Москва").address("ул. Тест, 1").distanceToCenter(1.0).build();

        room = Room.builder()
                .id(1L).name("Standard").number("101")
                .price(2000.0).maxGuests(2).hotel(hotel)
                .build();
    }

    @Test
    @DisplayName("isAvailable: нет броней → комната доступна")
    void isAvailable_noBookings_returnsTrue() {
        assertThat(room.isAvailable(
                LocalDate.of(2027, 6, 1),
                LocalDate.of(2027, 6, 5)
        )).isTrue();
    }

    @Test
    @DisplayName("isAvailable: запрашиваемые даты не пересекаются с бронью → доступна")
    void isAvailable_noOverlap_returnsTrue() {
        Booking existing = Booking.builder()
                .checkInDate(LocalDate.of(2027, 6, 10))
                .checkOutDate(LocalDate.of(2027, 6, 15))
                .build();
        room.getBookings().add(existing);

        assertThat(room.isAvailable(
                LocalDate.of(2027, 6, 1),
                LocalDate.of(2027, 6, 9)
        )).isTrue();
    }

    @Test
    @DisplayName("isAvailable: есть конфликтная бронь → недоступна")
    void isAvailable_conflictExists_returnsFalse() {
        Booking existing = Booking.builder()
                .checkInDate(LocalDate.of(2027, 6, 3))
                .checkOutDate(LocalDate.of(2027, 6, 8))
                .build();
        room.getBookings().add(existing);

        assertThat(room.isAvailable(
                LocalDate.of(2027, 6, 1),
                LocalDate.of(2027, 6, 5)
        )).isFalse();
    }

    @Test
    @DisplayName("isAvailable: checkIn == null → false (защита от NPE)")
    void isAvailable_nullCheckIn_returnsFalse() {
        assertThat(room.isAvailable(null, LocalDate.of(2027, 6, 5))).isFalse();
    }

    @Test
    @DisplayName("isAvailable: checkOut == null → false (защита от NPE)")
    void isAvailable_nullCheckOut_returnsFalse() {
        assertThat(room.isAvailable(LocalDate.of(2027, 6, 1), null)).isFalse();
    }

    @Test
    @DisplayName("isAvailable: checkIn после checkOut → false (невалидный диапазон)")
    void isAvailable_checkInAfterCheckOut_returnsFalse() {
        assertThat(room.isAvailable(
                LocalDate.of(2027, 6, 10),
                LocalDate.of(2027, 6, 5)
        )).isFalse();
    }
}