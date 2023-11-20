package com.argus.foodobserverbot.service.impl;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.repository.BotUserRepository;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.repository.FoodRecordRepository;
import com.argus.foodobserverbot.service.MainService;
import com.argus.foodobserverbot.service.enums.ServiceCommands;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.argus.foodobserverbot.entity.enums.UserState.*;

@Service
@Log4j2
public class MainServiceImpl implements MainService {

    private final BotUserRepository botUserRepository;

    private final DayRepository dayRepository;

    private final FoodRecordRepository foodRecordRepository;

    public MainServiceImpl(BotUserRepository botUserRepository, DayRepository dayRepository, FoodRecordRepository foodRecordRepository) {
        this.botUserRepository = botUserRepository;
        this.dayRepository = dayRepository;
        this.foodRecordRepository = foodRecordRepository;
    }

    @Override
    public String processTextMessage(Update update) {
        var botUser = findOrSaveAppUser(update);
        var userState = botUser.getUserState();
        var text = update.getMessage().getText();
        switch (userState) {
            case BASIC_STATE -> {
                return processServiceCommand(botUser, text);
            }
            case WAIT_FOR_INPUT_FOOD -> {
                var foodRecord = FoodRecord.builder()
                        .food(text)
                        .createdAt(LocalDateTime.now())
                        .creationDay(dayRepository.findByDate(LocalDate.now()).get())
                        .build();
                foodRecordRepository.save(foodRecord);
                botUser.setUserState(BASIC_STATE);
                botUserRepository.save(botUser);
                return "You added food record";
            }
            case WAIT_FOR_INPUT_BLOOD -> {
                var dayOptional = dayRepository.findByDate(LocalDate.now());
                var day = dayOptional.get();
                day.setBloodyRating(Integer.parseInt(text));
                dayRepository.save(day);
                return "Bloody rating is updated";
            }
            case WAIT_FOR_INPUT_PIMPLE -> {
                var dayOptional = dayRepository.findByDate(LocalDate.now());
                var day = dayOptional.get();
                day.setPimpleRating(Integer.parseInt(text));
                dayRepository.save(day);
                return "Pimple rating is updated";
            }
            default -> {
                log.error("Unknown user state " + userState);
                return "Unknown error! Enter /cancel and try again!";
            }
        }
    }

    private String processServiceCommand(BotUser botUser, String command) {
        try {
            var serviceCommand = ServiceCommands.getServiceCommandByValue(command);
            switch (Objects.requireNonNull(serviceCommand)) {
                case HELP -> {
                    return help();
                }
                case START -> {
                    return "Hello there! Enter /help to see available commands";
                }
                case DAY -> {
                    if (dayRepository.existsDayByDateIs(LocalDate.now())) {
                        return "You have already started this day's record";
                    } else {
                        var day = Day.builder()
                                .date(LocalDate.now())
                                .creator(botUser)
                                .build();
                        dayRepository.save(day);
                        return "You have started this day's record";
                    }
                }
                case FOOD_RECORD -> {
                    if (dayRepository.existsDayByDateIs(LocalDate.now())) {
                        botUser.setUserState(WAIT_FOR_INPUT_FOOD);
                        botUserRepository.save(botUser);
                        return "Enter food";
                    } else {
                        var day = Day.builder()
                                .date(LocalDate.now())
                                .creator(botUser)
                                .build();
                        dayRepository.save(day);
                        botUser.setUserState(WAIT_FOR_INPUT_FOOD);
                        botUserRepository.save(botUser);
                        return "You have started this day's record. Enter food";
                    }
                }
                case IS_BLOOD -> {
                    var dayOptional = dayRepository.findByDate(LocalDate.now());
                    if (dayOptional.isPresent()) {
                        botUser.setUserState(WAIT_FOR_INPUT_BLOOD);
                        botUserRepository.save(botUser);
                        return "How bloody is the poop? From 0 to 10";
                    } else {
                        return "You haven't started today's record!";
                    }
                }
                case IS_PIMPLE -> {
                    var dayOptional = dayRepository.findByDate(LocalDate.now());
                    if (dayOptional.isPresent()) {
                        botUser.setUserState(WAIT_FOR_INPUT_PIMPLE);
                        botUserRepository.save(botUser);
                        return "How much pimples? From 0 to 10";
                    } else {
                        return "You haven't started today's record!";
                    }
                }
                case CANCEL -> {
                    return cancelProcess(botUser);
                }
            }
            return unknown();
        } catch (RuntimeException e) {
            return unknown();
        }
    }

    private BotUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        BotUser persistentBotUser = botUserRepository.findBotUserByTelegramId(telegramUser.getId());
        if (persistentBotUser == null) {
            BotUser transientAppUser = BotUser.builder()
                    .telegramId(telegramUser.getId())
                    .name(telegramUser.getUserName())
                    .userState(BASIC_STATE)
                    .build();
            return botUserRepository.save(transientAppUser);
        }
        return persistentBotUser;
    }

    private String help() {
        return """
                List of available commands:\s
                /cancel - cancel current command;
                /day - start today's record;
                /food - add food record;
                /blood - changes the day to bloody;
                /pimple - changes the day to pimple""";
    }

    private String unknown() {
        return "Unknown command! Enter /help to see available commands";
    }

    private String cancelProcess(BotUser botUser) {
        botUser.setUserState(BASIC_STATE);
        botUserRepository.save(botUser);
        return "Command canceled!";
    }
}
