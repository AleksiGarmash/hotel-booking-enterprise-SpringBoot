package com.example.spring.booking.statistic.service;

import com.example.spring.booking.statistic.document.StatisticDocument;
import com.example.spring.booking.statistic.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final StatisticRepository statisticRepository;

    public List<StatisticDocument> findAll() {
        return statisticRepository.findAll();
    }

    public List<StatisticDocument> findByEventType(String eventType) {
        return statisticRepository.findByEventType(eventType);
    }

    public List<StatisticDocument> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return statisticRepository.findByTimestampBetween(from, to);
    }

    public List<StatisticDocument> findByUserId(Long userId) {
        return statisticRepository.findByUserId(userId);
    }

    public byte[] exportStatisticsToCsv() throws IOException {
        List<StatisticDocument> statistics = statisticRepository.findAll();
        return generateCsv(statistics);
    }

    public byte[] exportStatisticsByTypeToCsv(String eventType) throws IOException {
        List<StatisticDocument> statistics = statisticRepository.findByEventType(eventType);
        return generateCsv(statistics);
    }

    public byte[] exportStatisticsByDateRange(LocalDateTime from, LocalDateTime to) throws IOException {
        List<StatisticDocument> statistics = statisticRepository.findByTimestampBetween(from, to);
        return generateCsv(statistics);
    }

    private byte[] generateCsv(List<StatisticDocument> statistics) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("Event ID", "Event TYPE", "Timestamp", "User ID", "Username", "Email",
                        "Booking ID", "Room ID", "Hotel ID", "Check-in Date", "Check-out Date", "total Price")
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            for (StatisticDocument doc : statistics) {
                printer.printRecord(
                        doc.getEventId(),
                        doc.getEventType(),
                        doc.getTimestamp() != null ? doc.getTimestamp().format(formatter) : "",
                        doc.getUserId(),
                        doc.getUsername(),
                        doc.getEmail(),
                        doc.getBookingId(),
                        doc.getRoomId(),
                        doc.getHotelId(),
                        doc.getCheckInDate(),
                        doc.getCheckOutDate(),
                        doc.getTotalPrice()
                );
            }
            printer.flush();
        }
        return outputStream.toByteArray();
    }
}
