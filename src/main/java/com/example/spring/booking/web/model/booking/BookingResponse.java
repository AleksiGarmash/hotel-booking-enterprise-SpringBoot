package com.example.spring.booking.web.model.booking;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingResponse {
    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long roomId;
    private String roomName;
    private String roomNumber;
    private Long hotelId;
    private String hotelName;
    private Long userId;
    private String username;
    private Double totalPrice;
}
