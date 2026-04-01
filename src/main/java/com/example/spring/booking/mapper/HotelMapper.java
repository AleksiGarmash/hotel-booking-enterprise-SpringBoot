package com.example.spring.booking.mapper;

import com.example.spring.booking.entity.Hotel;
import com.example.spring.booking.mapper.delegate.HotelMapperDelegate;
import com.example.spring.booking.web.model.hotel.HotelRequest;
import com.example.spring.booking.web.model.hotel.HotelResponse;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@DecoratedWith(HotelMapperDelegate.class)
@Mapper(componentModel = "spring")
public interface HotelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "numberOfRatings", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    Hotel requestToHotel(HotelRequest request);

    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "numberOfRatings", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    @Mapping(source = "hotelId", target = "id")
    Hotel requestToHotel(Long hotelId, HotelRequest request);

    HotelResponse hotelToResponse(Hotel hotel);
}
