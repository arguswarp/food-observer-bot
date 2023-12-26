package com.argus.foodobserverbot.repository;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DayRepository extends JpaRepository<Day, Long> {
    Boolean existsDayByDateIs(LocalDate date);

    Optional<Day> findByDate(LocalDate date);

    List<Day> findByCreatorOrderByDateDesc(BotUser creator);

    List<Day> findAllByOrderByDateDesc();
}
