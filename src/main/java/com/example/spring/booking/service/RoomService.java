package com.example.spring.booking.service;

import com.example.spring.booking.entity.Hotel;
import com.example.spring.booking.entity.Room;
import com.example.spring.booking.exception.InvalidDataException;
import com.example.spring.booking.exception.ResourceNotFoundException;
import com.example.spring.booking.mapper.RoomMapper;
import com.example.spring.booking.repository.HotelRepository;
import com.example.spring.booking.repository.RoomRepository;
import com.example.spring.booking.specification.RoomSpecification;
import com.example.spring.booking.web.model.room.RoomRequest;
import com.example.spring.booking.web.model.room.RoomResponse;
import com.example.spring.booking.web.model.room.RoomSearchFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    public Page<RoomResponse> findAll(Pageable pageable) {
        log.info("Get Rooms with pagination: page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());

        return roomRepository.findAll(pageable)
                .map(roomMapper::roomToResponse);
    }

    public RoomResponse findById(Long id) {
        log.info("Get Room by ID: {}", id);

        return roomMapper.roomToResponse(
                roomRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                MessageFormat.format("Room with ID {} not found", id)
                        )
                )
        );
    }

    @Transactional
    public RoomResponse create(RoomRequest request) {
        log.info("Creating new Room: {}", request.getName());

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                MessageFormat.format("Hotel with ID {} not found", request.getHotelId())));

        if (roomRepository.existsByHotelIdAndNumber(request.getHotelId(), request.getNumber())) {
            throw new InvalidDataException(MessageFormat.format("Room with number {} already exist!", request.getNumber()));
        }

        Room room = roomMapper.requestToRoom(request);
        room.setHotel(hotel);

        Room savedRoom = roomRepository.save(room);
        log.info("Created Room with ID: {}", savedRoom.getId());

        return roomMapper.roomToResponse(savedRoom);
    }

    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        log.info("Updating Room with ID: {}", id);

        Room room = roomRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageFormat.format("Hotel with ID {} not found", request.getHotelId())));

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageFormat.format("Hotel with ID {} not found", request.getHotelId())));

        if (!room.getNumber().equals(request.getNumber()) &&
            roomRepository.existsByHotelIdAndNumber(room.getHotel().getId(), room.getNumber())) {
            throw new InvalidDataException(MessageFormat.format("Room with number {} already exist!", request.getNumber()));
        }

        room.setName(request.getName());
        room.setDescription(request.getDescription());
        room.setNumber(request.getNumber());
        room.setPrice(request.getPrice());
        room.setMaxGuests(request.getMaxGuests());
        room.setHotel(hotel);

        Room updatedRoom = roomRepository.save(room);
        log.info("Room updated");

        return roomMapper.roomToResponse(updatedRoom);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting Room with ID: {}", id);

        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException(MessageFormat.format("Room with ID {} not found", id));
        }

        roomRepository.deleteById(id);
        log.info("Room deleted");
    }

    public List<RoomResponse> searchRooms(RoomSearchFilter filter) {
        log.info("Search Rooms with filters: {}", filter);

        Specification<Room> spec = RoomSpecification.withFilters(filter);
        List<Room> rooms = roomRepository.findAll(spec);

        return rooms.stream()
                .map(roomMapper::roomToResponse)
                .collect(Collectors.toList());
    }

    public Page<RoomResponse> searchRooms(RoomSearchFilter filter, Pageable pageable) {
        log.info("Search Rooms with pagination and filters: {}", filter);

        Specification<Room> spec = RoomSpecification.withFilters(filter);
        return roomRepository.findAll(spec, pageable)
                .map(roomMapper::roomToResponse);
    }

    private void validateSearchDates(RoomSearchFilter filter) {
        if (filter.getCheckInDate() != null && filter.getCheckOutDate() != null) {
            if (filter.getCheckInDate().isAfter(filter.getCheckOutDate())) {
                throw new InvalidDataException("Check-in date can't be later then Check-out date!");
            }
            if (filter.getCheckOutDate().isBefore(filter.getCheckInDate())) {
                throw new InvalidDataException("Check-out date can't be before then Check-in date!");
            }
        } else if (filter.getCheckInDate() != null || filter.getCheckOutDate() != null) {
            log.info("Specified only one date, the date filter isn't applied");
            filter.setCheckInDate(null);
            filter.setCheckOutDate(null);
        }
    }
}
