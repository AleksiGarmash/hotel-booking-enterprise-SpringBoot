package com.example.spring.booking.web.model.hotel;

import lombok.Data;

@Data
public class HotelSearchFilter {
    private Long id;
    private String name;
    private String title;
    private String city;
    private String address;
    private Double minDistanceToCenter;
    private Double maxDistanceToCenter;
    private Double minRating;
    private Double maxRating;
}
