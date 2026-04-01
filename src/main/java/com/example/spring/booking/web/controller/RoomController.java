package com.example.spring.booking.web.controller;

import com.example.spring.booking.service.RoomService;
import com.example.spring.booking.web.model.room.RoomRequest;
import com.example.spring.booking.web.model.room.RoomResponse;
import com.example.spring.booking.web.model.room.RoomSearchFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<Page<RoomResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(roomService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RoomResponse> create(@RequestBody @Valid RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> update(@PathVariable Long id, @RequestBody @Valid RoomRequest request) {
        return ResponseEntity.ok(roomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/search/all")
    public ResponseEntity<List<RoomResponse>> searchAll(RoomSearchFilter filter) {
        return ResponseEntity.ok(roomService.searchRooms(filter));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<RoomResponse>> searchAll(RoomSearchFilter filter, Pageable pageable) {
        return ResponseEntity.ok(roomService.searchRooms(filter, pageable));
    }
}
