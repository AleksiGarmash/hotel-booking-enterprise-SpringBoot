package com.example.spring.booking.statistic.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticDocument {

    @Id
    private String id;

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private Long userId;
    private String username;
    private String email;

    private Long bookingId;
    private Long roomId;
    private Long hotelId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Double totalPrice;
}
