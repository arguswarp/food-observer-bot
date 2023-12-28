package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.exception.DatabaseException;
import com.argus.foodobserverbot.repository.DayRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.function.BiConsumer;

@Service
@Log4j2
@Transactional
public class DayService {
    private final DayRepository dayRepository;

    public DayService(DayRepository dayRepository) {
        this.dayRepository = dayRepository;
    }

    public Day findOrSaveDay(BotUser botUser, LocalDate date) {
        if (!dayRepository.existsDayByDateIsAndCreator(date, botUser)) {
            var day = Day.builder()
                    .date(date)
                    .creator(botUser)
                    .bloodyRating(0)
                    .pimpleFaceRating(0)
                    .pimpleBootyRating(0)
                    .notes("")
                    .build();
            log.info("User " + botUser.getName() + " started day record on " + day.getDate());
            return dayRepository.save(day);
        }
        return dayRepository.findByDateAndCreator(date, botUser).orElseThrow(() -> new DatabaseException("Can't save or find day"));
    }

    public Day setDayRating(int rating, LocalDate date, BotUser botUser, BiConsumer<Integer, Day> dayConsumer) {
        var dayOptional = dayRepository.findByDateAndCreator(date, botUser);
        var day = dayOptional.orElseThrow(() -> new DatabaseException("Can't find today"));
        dayConsumer.accept(rating, day);
        return dayRepository.save(day);
    }

    public Day addNote(String note, LocalDate date, BotUser botUser) {
        var dayOptional = dayRepository.findByDateAndCreator(date, botUser);
        var day = dayOptional.orElseThrow(() -> new DatabaseException("Can't find today"));
        String noteTransient = day.getNotes();
        noteTransient +=note + "\n";
        day.setNotes(noteTransient);
        return dayRepository.save(day);
    }
}
