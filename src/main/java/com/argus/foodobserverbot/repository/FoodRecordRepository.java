package com.argus.foodobserverbot.repository;

import com.argus.foodobserverbot.entity.FoodRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRecordRepository extends JpaRepository<FoodRecord, Long> {
}
