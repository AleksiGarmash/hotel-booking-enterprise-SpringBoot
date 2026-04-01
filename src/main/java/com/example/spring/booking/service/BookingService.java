package com.example.spring.booking.service;

import com.example.spring.booking.entity.Booking;
import com.example.spring.booking.entity.Room;
import com.example.spring.booking.entity.User;
import com.example.spring.booking.exception.ResourceNotFoundException;
import com.example.spring.booking.exception.RoomNotAvailableException;
import com.example.spring.booking.mapper.BookingMapper;
import com.example.spring.booking.repository.BookingRepository;
import com.example.spring.booking.repository.RoomRepository;
import com.example.spring.booking.repository.UserRepository;
import com.example.spring.booking.statistic.event.BookingEvent;
import com.example.spring.booking.statistic.producer.KafkaEventPublisher;
import com.example.spring.booking.web.model.booking.BookingListResponse;
import com.example.spring.booking.web.model.booking.BookingRequest;
import com.example.spring.booking.web.model.booking.BookingResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final KafkaEventPublisher kafkaEventPublisher;

    public BookingListResponse findAll() {
        log.info("Get all Bookings");
        return bookingMapper.bookingsToListResponse(bookingRepository.findAll());
    }

    public BookingListResponse findAll(Pageable pageable) {
        log.info("Get all Booking with Pagination");

        Page<Booking> page = bookingRepository.findAll(pageable);
        return bookingMapper.bookingsToListResponse(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }

    public BookingResponse findById(Long id) {
        log.info("Get Booking by ID: {}", id);

        return bookingMapper.bookingToResponse(bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Booking with ID {} not found", id))));
    }

    public List<BookingResponse> findByRoomId(Long roomId) {
        log.info("Get Booking by Room ID: {}", roomId);
        return bookingMapper.bookingsToResponseList(bookingRepository.findByRoomId(roomId));
    }

    public List<BookingResponse> findByUserId(Long userId) {
        log.info("Get Booking by User ID: {}", userId);
        return bookingMapper.bookingsToResponseList(bookingRepository.findByUserId(userId));
    }

    @Transactional
    public BookingResponse create(BookingRequest request) {
        log.info("Creating Booking: Room ID {}, User ID {}, Date {} - {}",
                request.getRoomId(), request.getUserId(), request.getCheckInDate(), request.getCheckOutDate());

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Room with ID {} not found!", request.getRoomId())));

        User user = userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("User with ID {} not found!", request.getUserId())));

        validateDates(request.getCheckInDate(), request.getCheckOutDate());
        if (!isRoomAvailable(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate())) {
            throw new RoomNotAvailableException("Room unavailable for those dates!");
        }

        Booking booking = bookingMapper.requestToBooking(request);
        booking.setRoom(room);
        booking.setUser(user);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking saved");

        BookingResponse response = bookingMapper.bookingToResponse(savedBooking);
        BookingEvent event = BookingEvent.builder()
                .bookingId(savedBooking.getId())
                .userId(savedBooking.getUser().getId())
                .roomId(savedBooking.getRoom().getId())
                .hotelId(savedBooking.getRoom().getHotel().getId())
                .checkInDate(savedBooking.getCheckInDate())
                .checkOutDate(savedBooking.getCheckOutDate())
                .totalPrice(response.getTotalPrice())
                .timestamp(LocalDateTime.now())
                .build();
        kafkaEventPublisher.publishBookingEvent(event);

        return response;
    }

    @Transactional
    public BookingResponse update(Long id, BookingRequest request) {
        log.info("Updating Booking: {}, new dates: {} - {}",
                id, request.getCheckInDate(), request.getCheckOutDate());

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Booking with ID {} not found!", id)));

        Room room;
        if (request.getRoomId() != null && !request.getRoomId().equals(booking.getRoom().getId())) {
            room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Room with ID {} not found!", request.getRoomId())));
        } else {
            room = booking.getRoom();
        }

        User user;
        if (request.getUserId() != null && !request.getUserId().equals(booking.getUser().getId())) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("User with ID {} not found!", request.getUserId())));
        } else {
            user = booking.getUser();
        }

        validateDates(request.getCheckInDate(), request.getCheckOutDate());
        if (!isRoomAvailable(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate())) {
            throw new RoomNotAvailableException("Room unavailable for those dates!");
        }

        booking.setRoom(room);
        booking.setUser(user);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking updated");

        return bookingMapper.bookingToResponse(updatedBooking);
    }

    @Transactional
    public void cancel(Long id) {
        log.info("Canceling Booking: {}", id);

        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException(MessageFormat.format("Booking with ID {} not found!", id));
        }

        bookingRepository.deleteById(id);
    }

    public Page<BookingResponse> searchWithFilter(
            Long userId,
            Long roomId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        log.info("Search Booking with pagination and filters");

        Specification<Booking> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }

            if (roomId != null) {
                predicates.add(criteriaBuilder.equal(root.get("room").get("id"), roomId));
            }

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("checkInDate"), fromDate));
            }

            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("checkOutDate"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return bookingRepository.findAll(spec, pageable)
                .map(bookingMapper::bookingToResponse);
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check out dates must be specified!");
        }

        if (checkIn.isAfter(checkOut)) {
            throw new IllegalArgumentException("Check-in date can't be later then Check-out date!");
        }

        if (checkIn.isEqual(checkOut)) {
            throw new IllegalArgumentException("Booking period minimum for 1 day!");
        }
    }

    private boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> conflictBookings = bookingRepository.findConflictBooking(roomId, checkIn, checkOut);
        return conflictBookings.isEmpty();
    }
}
