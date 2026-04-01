package com.example.spring.booking.mapper.delegate;

import com.example.spring.booking.entity.Hotel;
import com.example.spring.booking.mapper.HotelMapper;
import com.example.spring.booking.web.model.hotel.HotelRequest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class HotelMapperDelegate implements HotelMapper {

    @Autowired
    private HotelMapper hotelMapper;

    @Override
    public Hotel requestToHotel(HotelRequest request) {
        Hotel hotel = hotelMapper.requestToHotel(request);
        hotel.setRating(0.0);
        hotel.setNumberOfRatings(0);
        return hotel;
    }

    @Override
    public Hotel requestToHotel(Long hotelId, HotelRequest request) {
        Hotel hotel = hotelMapper.requestToHotel(hotelId, request);
        hotel.setId(hotelId);
        return hotel;
    }
}
