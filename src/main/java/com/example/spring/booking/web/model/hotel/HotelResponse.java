package com.example.spring.booking.web.model.hotel;

import lombok.Data;

@Data
public class HotelResponse {
    private Long id;
    private String name;
    private String title;
    private String city;
    private String address;
    private Double distanceToCenter;
    private Double rating;
    private Integer numberOfRatings;
}
