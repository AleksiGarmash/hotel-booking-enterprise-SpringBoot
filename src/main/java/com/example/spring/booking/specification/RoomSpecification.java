package com.example.spring.booking.specification;

import com.example.spring.booking.entity.Booking;
import com.example.spring.booking.entity.Room;
import com.example.spring.booking.web.model.room.RoomSearchFilter;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RoomSpecification {

    public static Specification<Room> withFilters(RoomSearchFilter filter) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), filter.getId()));
            }

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filter.getDescription())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + filter.getDescription().toLowerCase() + "%"));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"),
                        filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"),
                        filter.getMaxPrice()));
            }

            if (filter.getMinGuests() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("maxGuests"),
                        filter.getMinGuests()));
            }

            if (filter.getMaxGuests() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("maxGuests"),
                        filter.getMaxGuests()));
            }

            if (filter.getHotelId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("hotel").get("id"),filter.getHotelId()));
            }

            if (filter.getCheckInDate() != null && filter.getCheckOutDate() != null) {
                if (!filter.getCheckInDate().isAfter(filter.getCheckOutDate())) {

                    Subquery<Long> bookingSubquery = query.subquery(Long.class);
                    Root<Booking> bookingRoot = bookingSubquery.from(Booking.class);

                    Predicate roomMatch = criteriaBuilder.equal(bookingRoot.get("room"), root);
                    Predicate dateOverlap = criteriaBuilder.and(
                            criteriaBuilder.lessThanOrEqualTo(bookingRoot.get("checkInDate"), filter.getCheckInDate()),
                            criteriaBuilder.greaterThanOrEqualTo(bookingRoot.get("checkOutDate"), filter.getCheckOutDate())
                    );

                    bookingSubquery.select(bookingRoot.get("id"))
                            .where(criteriaBuilder.and(roomMatch, dateOverlap));

                    predicates.add(criteriaBuilder.not(criteriaBuilder.exists(bookingSubquery)));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
