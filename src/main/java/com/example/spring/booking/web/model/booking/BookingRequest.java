package com.example.spring.booking.web.model.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {

    @NotNull(message = "Room ID must be specified!")
    private Long roomId;

    @NotNull(message = "User ID must be specified!")
    private Long userId;

    @NotNull(message = "Check-in Date must be specified!")
    @FutureOrPresent(message = "Check-in Date must be in the present or future!")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date must be specified!")
    @Future(message = "Check-out Date must be in the future!")
    private LocalDate checkOutDate;
}
