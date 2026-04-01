package com.example.spring.booking.web.model.hotel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class HotelRequest {
        @NotBlank(message = "Name must be specified!")
        private String name;

        @NotBlank(message = "Title must specified!")
        private String title;

        @NotBlank(message = "City must be specified!")
        private String city;

        @NotBlank(message = "Address must be specified!")
        private String address;

        @NotNull(message = "Distance to Center must be specified!")
        @Positive(message = "Distance must be positive!")
        private Double distanceToCenter;
}
