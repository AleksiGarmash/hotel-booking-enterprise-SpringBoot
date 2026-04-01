package com.example.spring.booking.mapper;

import com.example.spring.booking.entity.Booking;
import com.example.spring.booking.entity.Room;
import com.example.spring.booking.entity.User;
import com.example.spring.booking.mapper.delegate.BookingMapperDelegate;
import com.example.spring.booking.web.model.booking.BookingListResponse;
import com.example.spring.booking.web.model.booking.BookingRequest;
import com.example.spring.booking.web.model.booking.BookingResponse;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@DecoratedWith(BookingMapperDelegate.class)
@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "room", source = "roomId", qualifiedByName = "roomFromId")
    @Mapping(target = "user", source = "userId", qualifiedByName = "userFromId")
    Booking requestToBooking(BookingRequest request);

    @Mapping(source = "bookingId", target = "id")
    @Mapping(target = "room", source = "request.roomId", qualifiedByName = "roomFromId")
    @Mapping(target = "user", source = "request.userId", qualifiedByName = "userFromId")
    Booking requestToBooking(Long bookingId, BookingRequest request);

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomName", source = "room.name")
    @Mapping(target = "roomNumber", source = "room.number")
    @Mapping(target = "hotelId", source = "room.hotel.id")
    @Mapping(target = "hotelName", source = "room.hotel.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "totalPrice", expression = "java(calculateTotalPrice(booking))")
    BookingResponse bookingToResponse(Booking booking);

    List<BookingResponse> bookingsToResponseList(List<Booking> bookings);

    default BookingListResponse bookingsToListResponse(List<Booking> bookings) {
        BookingListResponse response = new BookingListResponse();
        response.setBookings(bookings.stream()
                .map(this::bookingToResponse)
                .collect(Collectors.toList()));

        return response;
    }

    default BookingListResponse bookingsToListResponse(
            List<Booking> bookings, long totalElement, int totalPages, int currentPages) {
        BookingListResponse response = new BookingListResponse();
        response.setBookings(bookings.stream()
                .map(this::bookingToResponse)
                .collect(Collectors.toList()));
        response.setTotalElement(totalElement);
        response.setTotalPages(totalPages);
        response.setCurrentPages(currentPages);

        return response;
    }

    @Named("roomFromId")
    default Room roomFromId(Long roomId) {
        if (roomId == null) return null;
        Room room = new Room();
        room.setId(roomId);
        return room;
    }

    @Named("userFromId")
    default User userFromId(Long userId) {
        if (userId == null) return  null;
        User user = new User();
        user.setId(userId);
        return user;
    }

    default Double calculateTotalPrice(Booking booking) {
        if (booking == null || booking.getRoom() == null ||
            booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
            return 0.0;
        }

        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        return booking.getRoom().getPrice() * days;
    }
}
