package com.example.spring.booking.service;

import com.example.spring.booking.entity.Hotel;
import com.example.spring.booking.exception.InvalidDataException;
import com.example.spring.booking.exception.ResourceNotFoundException;
import com.example.spring.booking.mapper.HotelMapper;
import com.example.spring.booking.repository.HotelRepository;
import com.example.spring.booking.specification.HotelSpecification;
import com.example.spring.booking.web.model.hotel.HotelRequest;
import com.example.spring.booking.web.model.hotel.HotelResponse;
import com.example.spring.booking.web.model.hotel.HotelSearchFilter;
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
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    public Page<HotelResponse> findAll(Pageable pageable) {
        log.info("Get Hotels with pagination: page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());

        return hotelRepository.findAll(pageable)
                .map(hotelMapper::hotelToResponse);
    }

    public HotelResponse findById(Long id) {
        log.info("Get Hotel by ID: {}", id);

        return hotelMapper.hotelToResponse(
                hotelRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                MessageFormat.format("Hotel with ID {} not found", id))));
    }


    @Transactional
    public HotelResponse create(HotelRequest request) {
        Hotel hotel = hotelMapper.requestToHotel(request);
        Hotel saved = hotelRepository.save(hotel);
        log.info("Creating new Hotel: {}, {}", saved.getId(), request.getName());

        return hotelMapper.hotelToResponse(saved);
    }

    @Transactional
    public HotelResponse update(Long id, HotelRequest request) {
        log.info("Updating Hotel with ID: {}", id);

        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                MessageFormat.format("Hotel with ID {} not found", id)));

        Double currentRating = hotel.getRating();
        Integer currentNumberOfRating = hotel.getNumberOfRatings();

        hotel.setName(request.getName());
        hotel.setTitle(request.getTitle());
        hotel.setCity(request.getCity());
        hotel.setAddress(request.getAddress());
        hotel.setDistanceToCenter(request.getDistanceToCenter());

        hotel.setRating(currentRating);
        hotel.setNumberOfRatings(currentNumberOfRating);

        Hotel updatedHotel = hotelRepository.save(hotel);
        log.info("Hotel updated");

        return hotelMapper.hotelToResponse(updatedHotel);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting Hotel with ID: {}", id);

        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException(MessageFormat.format("Hotel with ID {} not found", id));
        }

        hotelRepository.deleteById(id);
        log.info("Hotel deleted");
    }

    @Transactional
    public HotelResponse updateRating(Long id, Integer assessment) {
        log.info("Updating Hotel rating: {}", id);

        if (assessment < 1 || assessment > 5) {
            throw new InvalidDataException("Assessment must be from 1 to 5!");
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Hotel with ID {} mot found!", id)));

        Double currentRating = hotel.getRating();
        Integer currentNumberOfRating = hotel.getNumberOfRatings();

        if (currentNumberOfRating == 0) {
            //first assessment
            hotel.setRating(Double.valueOf(assessment));
            hotel.setNumberOfRatings(1);
        } else {
            //1. sum of ratings
            Double totalRating = currentRating * currentNumberOfRating;

            //2. current rating + assessment
            totalRating = totalRating - currentRating + assessment;

            //3. new avg rating
            Double newRating = totalRating / currentNumberOfRating;

            // round to 0,1
            newRating = Math.round(newRating * 10) / 10.0;

            //4. raising number os rating
            hotel.setNumberOfRatings(currentNumberOfRating + 1);
            hotel.setRating(newRating);
        }

        Hotel updatedHotel = hotelRepository.save(hotel);
        log.info("Rating of Hotel {} updated. New rating: {}", id, updatedHotel.getRating());
        return hotelMapper.hotelToResponse(updatedHotel);
    }

    public List<HotelResponse> searchHotel(HotelSearchFilter filter) {
        log.info("Search Hotels with filters: {}", filter);

        Specification<Hotel> spec = HotelSpecification.withFilters(filter);
        List<Hotel> hotels = hotelRepository.findAll(spec);

        return hotels.stream()
                .map(hotelMapper::hotelToResponse)
                .collect(Collectors.toList());
    }

    public Page<HotelResponse> searchHotel(HotelSearchFilter filter, Pageable pageable) {
        log.info("Search Hotels with pagination and filter: {}", filter);

        Specification<Hotel> spec = HotelSpecification.withFilters(filter);
        return hotelRepository.findAll(spec, pageable)
                .map(hotelMapper::hotelToResponse);
    }
}


