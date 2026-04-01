package com.example.spring.booking.statistic.controller;

import com.example.spring.booking.statistic.document.StatisticDocument;
import com.example.spring.booking.statistic.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public ResponseEntity<List<StatisticDocument>> findAll() {
        return ResponseEntity.ok(statisticsService.findAll());
    }

    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<StatisticDocument>> findByEventType(@PathVariable String eventType) {
        return ResponseEntity.ok(statisticsService.findByEventType(eventType));
    }

    @GetMapping("/range")
    public ResponseEntity<List<StatisticDocument>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
            ) {
        return ResponseEntity.ok(statisticsService.findByDateRange(from, to));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportToCsv() throws IOException {
        byte[] csvData = statisticsService.exportStatisticsToCsv();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("text/csv"));
        httpHeaders.setContentDispositionFormData("attachment", "statistics.csv");
        httpHeaders.setContentLength(csvData.length);

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(csvData);
    }

    @GetMapping("/export/csv/type/{eventType}")
    public ResponseEntity<byte[]> exportByTypeToCsv(@PathVariable String eventType) throws IOException {
        byte[] csvData = statisticsService.exportStatisticsByTypeToCsv(eventType);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("text/csv"));
        httpHeaders.setContentDispositionFormData("attachment", "statistics_" + eventType + ".csv");
        httpHeaders.setContentLength(csvData.length);

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(csvData);
    }

    @GetMapping("/export/csv/range")
    public ResponseEntity<byte[]> exportByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) throws IOException {
        byte[] csvData = statisticsService.exportStatisticsByDateRange(from, to);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType("text/csv"));
        httpHeaders.setContentDispositionFormData("attachment", "statistics_" + from + "_to_" + to + ".csv");
        httpHeaders.setContentLength(csvData.length);

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(csvData);
    }
}
