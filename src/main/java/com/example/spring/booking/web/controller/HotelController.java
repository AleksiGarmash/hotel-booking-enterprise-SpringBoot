package com.example.spring.booking.web.controller;

import com.example.spring.booking.mapper.HotelMapper;
import com.example.spring.booking.repository.HotelRepository;
import com.example.spring.booking.service.HotelService;
import com.example.spring.booking.web.model.hotel.HotelRequest;
import com.example.spring.booking.web.model.hotel.HotelResponse;
import com.example.spring.booking.web.model.hotel.HotelSearchFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public ResponseEntity<Page<HotelResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(hotelService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.findById(id));
    }

    @PostMapping
    public ResponseEntity<HotelResponse> create(@RequestBody @Valid HotelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hotelService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelResponse> update(@PathVariable Long id, @RequestBody @Valid HotelRequest request) {
        return ResponseEntity.ok(hotelService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rating")
    public ResponseEntity<HotelResponse> rateHotel(@PathVariable Long id, @RequestParam @Min(1) @Max(5) Integer assessment) {
        return ResponseEntity.ok(hotelService.updateRating(id, assessment));
    }

    @GetMapping("/search/all")
    public ResponseEntity<List<HotelResponse>> searchAll(HotelSearchFilter filter) {
        return ResponseEntity.ok(hotelService.searchHotel(filter));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<HotelResponse>> searchAll(HotelSearchFilter filter, Pageable pageable) {
        return ResponseEntity.ok(hotelService.searchHotel(filter, pageable));
    }
}
