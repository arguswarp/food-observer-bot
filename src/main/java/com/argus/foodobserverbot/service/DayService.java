package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.exception.DatabaseException;
import com.argus.foodobserverbot.repository.DayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DayService {
    private final DayRepository dayRepository;


    public DayService(DayRepository dayRepository) {
        this.dayRepository = dayRepository;
    }
    @Transactional
    public Day findOrSaveDay(BotUser botUser, LocalDate date) {
        if (!dayRepository.existsDayByDateIs(date)) {
            var day = Day.builder()
                    .date(date)
                    .creator(botUser)
                    .bloodyRating(0)
                    .pimpleFaceRating(0)
                    .pimpleBootyRating(0)
                    .build();
            return dayRepository.save(day);
        }
        return dayRepository.findByDate(date).orElseThrow(() -> new DatabaseException("Can't save or find day"));
    }
}
