package com.example.spring.booking.web.model.room;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RoomRequest {

    @NotBlank(message = "Name must be specified!")
    private String name;

    private String description;

    @NotBlank(message = "Number must be specified!")
    private String number;

    @NotNull(message = "Price must be specified!")
    @Positive(message = "Price must be positive!")
    private Double price;

    @NotNull(message = "Guests must be specified!")
    @Min(value = 1, message = "Min 1 Guest!")
    @Max(value = 10, message = "Max 10 guests!")
    private Integer maxGuests;

    @NotNull(message = "Hotel ID must be specified!")
    private Long hotelId;
}
