package com.example.spring.booking.statistic.repository;

import com.example.spring.booking.statistic.document.StatisticDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatisticRepository extends MongoRepository<StatisticDocument, String> {
    List<StatisticDocument> findByEventType(String eventType);
    List<StatisticDocument> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
    List<StatisticDocument> findByUserId(Long userId);
}
