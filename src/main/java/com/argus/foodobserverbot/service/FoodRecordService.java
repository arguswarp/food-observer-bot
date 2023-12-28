package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.exception.DatabaseException;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.repository.FoodRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class FoodRecordService {
    private final FoodRecordRepository foodRecordRepository;
    private final DayRepository dayRepository;

    public FoodRecordService(FoodRecordRepository foodRecordRepository, DayRepository dayRepository) {
        this.foodRecordRepository = foodRecordRepository;
        this.dayRepository = dayRepository;
    }

    public FoodRecord addFood(String text, LocalDate date, BotUser botUser) {
        var day = dayRepository.findByDateAndCreator(date, botUser)
                .orElseThrow(() -> new DatabaseException("Can't find the day"));
        var foodRecord = FoodRecord.builder()
                .food(text)
                .createdAt(LocalDateTime.now())
                .creationDay(day)
                .build();
        return foodRecordRepository.save(foodRecord);
    }
}
