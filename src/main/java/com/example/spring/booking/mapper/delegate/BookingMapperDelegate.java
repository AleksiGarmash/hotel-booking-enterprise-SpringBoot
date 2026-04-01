package com.example.spring.booking.mapper.delegate;

import com.example.spring.booking.entity.Booking;
import com.example.spring.booking.entity.Room;
import com.example.spring.booking.entity.User;
import com.example.spring.booking.exception.ResourceNotFoundException;
import com.example.spring.booking.mapper.BookingMapper;
import com.example.spring.booking.repository.RoomRepository;
import com.example.spring.booking.repository.UserRepository;
import com.example.spring.booking.web.model.booking.BookingRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;

public abstract class BookingMapperDelegate implements BookingMapper {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Booking requestToBooking(BookingRequest request) {
        Booking booking = bookingMapper.requestToBooking(request);

        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Room with ID {} not found!", request.getRoomId())));
            booking.setRoom(room);
        }

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("User with ID {} nit found!", request.getUserId())));
            booking.setUser(user);
        }

        return booking;
    }

    @Override
    public Booking requestToBooking(Long bookingId, BookingRequest request) {
        Booking booking = bookingMapper.requestToBooking(bookingId, request);

        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Room with ID {} not found!", request.getRoomId())));
            booking.setRoom(room);
        }

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("User with ID {} no+t found!", request.getUserId())));
            booking.setUser(user);
        }

        return booking;
    }
}
