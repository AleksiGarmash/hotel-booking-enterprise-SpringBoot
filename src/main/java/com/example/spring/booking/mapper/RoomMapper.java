package com.example.spring.booking.mapper;

import com.example.spring.booking.entity.Hotel;
import com.example.spring.booking.entity.Room;
import com.example.spring.booking.mapper.delegate.RoomMapperDelegate;
import com.example.spring.booking.web.model.room.RoomRequest;
import com.example.spring.booking.web.model.room.RoomResponse;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@DecoratedWith(RoomMapperDelegate.class)
@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "hotel", source = "hotelId", qualifiedByName = "hotelFromId")
    Room requestToRoom(RoomRequest request);

    @Mapping(source = "roomId", target = "id")
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "hotel", source = "request.hotelId", qualifiedByName = "hotelFromId")
    Room requestToRoom(Long roomId, RoomRequest request);

    @Mapping(target = "hotelId", source = "hotel.id")
    @Mapping(target = "hotelName", source = "hotel.name")
    RoomResponse roomToResponse(Room room);

    @Named("hotelFromId")
    default Hotel hotelFromId(Long hotelId) {
        if (hotelId == null) { return null; }
        Hotel hotel = new Hotel();
        hotel.setId(hotelId);
        return hotel;
    }
}
