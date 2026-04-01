package com.example.spring.booking.mapper.delegate;

import com.example.spring.booking.entity.Hotel;
import com.example.spring.booking.entity.Room;
import com.example.spring.booking.exception.ResourceNotFoundException;
import com.example.spring.booking.mapper.RoomMapper;
import com.example.spring.booking.repository.HotelRepository;
import com.example.spring.booking.web.model.room.RoomRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;

public abstract class RoomMapperDelegate implements RoomMapper {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private HotelRepository hotelRepository;

    @Override
    public Room requestToRoom(RoomRequest request) {
        Room room = roomMapper.requestToRoom(request);

        if (request.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format(
                            "Hotel with ID {} not found!", request.getHotelId()
                    )));

            room.setHotel(hotel);
        }

        return room;
    }

    @Override
    public Room requestToRoom(Long roomId,RoomRequest request) {
        Room room = roomMapper.requestToRoom(roomId, request);

        if (request.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format(
                            "Hotel with ID {} not found!", request.getHotelId()
                    )));

            room.setHotel(hotel);
        }

        return room;
    }
}
