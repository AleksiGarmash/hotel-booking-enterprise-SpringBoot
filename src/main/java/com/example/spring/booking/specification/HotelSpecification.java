package com.example.spring.booking.specification;

import com.example.spring.booking.entity.Hotel;
import com.example.spring.booking.web.model.hotel.HotelSearchFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class HotelSpecification {

    public static Specification<Hotel> withFilters(HotelSearchFilter filter) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), filter.getId()));
            }

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filter.getTitle())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + filter.getTitle().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filter.getCity())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("city")),
                        filter.getCity().toLowerCase()));
            }

            if (StringUtils.hasText(filter.getAddress())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("address")),
                        "%" + filter.getAddress().toLowerCase() + "%"));
            }

            if (filter.getMinDistanceToCenter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("distanceToCenter"),
                        filter.getMinDistanceToCenter()));
            }

            if (filter.getMaxDistanceToCenter() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("distanceToCenter"),
                        filter.getMaxDistanceToCenter()));
            }

            if (filter.getMinRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rating"),
                        filter.getMinRating()));
            }

            if (filter.getMaxRating() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("rating"),
                        filter.getMaxRating()));
            }

            return criteriaBuilder.and(predicates.toArray(predicates.toArray(new Predicate[0])));
        };
    }
}
