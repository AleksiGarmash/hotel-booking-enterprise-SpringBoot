package com.example.spring.booking.repository;

import com.example.spring.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    List<Booking> findByRoomId(Long roomId);
    List<Booking> findByUserId(Long userId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.room.id = :roomId AND " +
            "((b.checkInDate BETWEEN :checkIn AND :checkOut) OR " +
            "(b.checkOutDate BETWEEN :checkIn AND :checkOut) OR " +
            "(b.checkInDate <= :checkIn AND b.checkOutDate >= :checkOut))")
    List<Booking> findConflictBooking(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /*boolean existByRoomIdAndCheckInLessAndCheckOutGreater(Long roomId, LocalDate checkIn, LocalDate checkOut);*/
}
