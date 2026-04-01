package com.example.spring.booking.web.controller;

import com.example.spring.booking.service.BookingService;
import com.example.spring.booking.web.model.booking.BookingListResponse;
import com.example.spring.booking.web.model.booking.BookingRequest;
import com.example.spring.booking.web.model.booking.BookingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/all")
    public ResponseEntity<BookingListResponse> findAll() {
        return ResponseEntity.ok(bookingService.findAll());
    }

    @GetMapping
    public ResponseEntity<BookingListResponse> findAll(Pageable pageable) {
        return ResponseEntity.ok(bookingService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.findById(id));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<BookingResponse>> findByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(bookingService.findByRoomId(roomId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> findByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@RequestBody @Valid BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> update(@PathVariable Long id, @RequestBody @Valid BookingRequest request) {
        return ResponseEntity.ok(bookingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        bookingService.cancel(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BookingResponse>> searchWithFilter(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable
            ) {
        return ResponseEntity.ok(bookingService.searchWithFilter(userId, roomId, fromDate, toDate, pageable));
    }
}
