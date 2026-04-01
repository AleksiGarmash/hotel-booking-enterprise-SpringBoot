package com.example.spring.booking.config;

import com.example.spring.booking.entity.*;
import com.example.spring.booking.repository.BookingRepository;
import com.example.spring.booking.repository.HotelRepository;
import com.example.spring.booking.repository.RoomRepository;
import com.example.spring.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Инициализация тестовых данных...");

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@hotel.com")
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);

            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .email("user@hotel.com")
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user);

            Hotel hotel1 = Hotel.builder()
                    .name("Grand Hotel")
                    .title("Роскошный отель в центре города")
                    .city("Москва")
                    .address("ул. Тверская, 1")
                    .distanceToCenter(0.5)
                    .rating(4.5)
                    .numberOfRatings(100)
                    .build();
            hotelRepository.save(hotel1);

            Hotel hotel2 = Hotel.builder()
                    .name("Park Inn")
                    .title("Уютный отель рядом с парком")
                    .city("Санкт-Петербург")
                    .address("Невский пр., 50")
                    .distanceToCenter(1.2)
                    .rating(4.2)
                    .numberOfRatings(75)
                    .build();
            hotelRepository.save(hotel2);

            Room room1 = Room.builder()
                    .name("Стандарт")
                    .description("Уютный номер с видом на город")
                    .number("101")
                    .price(5000.0)
                    .maxGuests(2)
                    .hotel(hotel1)
                    .build();
            roomRepository.save(room1);

            Room room2 = Room.builder()
                    .name("Люкс")
                    .description("Просторный номер с гостиной")
                    .number("201")
                    .price(10000.0)
                    .maxGuests(4)
                    .hotel(hotel1)
                    .build();
            roomRepository.save(room2);

            Room room3 = Room.builder()
                    .name("Эконом")
                    .description("Бюджетный вариант")
                    .number("15")
                    .price(3000.0)
                    .maxGuests(1)
                    .hotel(hotel2)
                    .build();
            roomRepository.save(room3);

            Booking booking = Booking.builder()
                    .checkInDate(LocalDate.now().plusDays(7))
                    .checkOutDate(LocalDate.now().plusDays(14))
                    .room(room1)
                    .user(user)
                    .build();
            bookingRepository.save(booking);

            log.info("Тестовые данные успешно созданы");
        }
    }
}
