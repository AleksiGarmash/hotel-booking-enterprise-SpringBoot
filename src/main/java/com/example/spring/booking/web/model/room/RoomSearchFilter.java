package com.example.spring.booking.web.model.room;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RoomSearchFilter {
    private Long id;
    private String name;
    private String description;
    private Double minPrice;
    private Double maxPrice;
    private Integer minGuests;
    private Integer maxGuests;
    private Long hotelId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutDate;
}