package com.example.spring.booking.web.model.room;

import lombok.Data;

@Data
public class RoomResponse {
    private Long id;
    private String name;
    private String description;
    private String number;
    private Double price;
    private Integer maxGuests;
    private Long hotelId;
    private String hotelName;
}
