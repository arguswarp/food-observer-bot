package com.argus.foodobserverbot.repository;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DayRepository extends JpaRepository<Day, Long> {
    Boolean existsDayByDateIsAndCreator(LocalDate date, BotUser creator);
    @EntityGraph(attributePaths = {"foodRecords"})
    Optional<Day> findByDateAndCreator(LocalDate date, BotUser creator);

    List<Day> findAllByOrderByDateDesc();

    List<Day> findByCreatorOrderByDateDesc(BotUser creator);
}
