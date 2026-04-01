package com.example.spring.booking.statistic.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
}
