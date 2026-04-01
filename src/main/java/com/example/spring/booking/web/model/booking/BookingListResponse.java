package com.example.spring.booking.web.model.booking;

import lombok.Data;

import java.util.List;

@Data
public class BookingListResponse {
    private List<BookingResponse> bookings;
    private long totalElement;
    private int totalPages;
    private int currentPages;
}
