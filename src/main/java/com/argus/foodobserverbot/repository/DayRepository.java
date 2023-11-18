package com.argus.foodobserverbot.repository;

import com.argus.foodobserverbot.entity.Day;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DayRepository extends JpaRepository<Day, Long> {
    Boolean existsDayByDateIs(LocalDate date);

    Optional<Day> findByDate(LocalDate date);
}
