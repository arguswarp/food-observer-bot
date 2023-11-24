package com.argus.foodobserverbot.service.impl;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.entity.enums.UserState;
import com.argus.foodobserverbot.exception.DatabaseException;
import com.argus.foodobserverbot.repository.BotUserRepository;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.repository.FoodRecordRepository;
import com.argus.foodobserverbot.service.MainService;
import com.argus.foodobserverbot.service.enums.ServiceCommands;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;

import static com.argus.foodobserverbot.entity.enums.UserState.*;
import static com.argus.foodobserverbot.service.enums.ServiceCommands.*;

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
    public String processText(BotUser botUser, String text) {
        var userState = botUser.getUserState();
        if (text.equals(CANCEL.getCommand())) {
            return cancel(botUser);
        }
        switch (userState) {
            case BASIC_STATE -> {
                return processServiceCommand(botUser, text);
            }
            case WAIT_FOR_INPUT_FOOD -> {
                var foodRecord = FoodRecord.builder()
                        .food(text)
                        .createdAt(LocalDateTime.now())
                        .creationDay(dayRepository.findByDate(LocalDate.now())
                                .orElseThrow(() -> new DatabaseException("Can't find today")))
                        .build();
                foodRecordRepository.save(foodRecord);
                botUser.setUserState(BASIC_STATE);
                botUserRepository.save(botUser);
                log.info("User " + botUser.getName()
                        + " called: new food input"
                        + " new state is: " + botUser.getUserState());
                return "You added food record";
            }
            case WAIT_FOR_INPUT_BLOOD -> {
                return setDayRatingToday(botUser,
                        day -> day.setBloodyRating(Integer.parseInt(text)),
                        "Bloody rating is updated");
            }
            case WAIT_FOR_INPUT_PIMPLE -> {
                return setDayRatingToday(botUser,
                        day -> day.setPimpleRating(Integer.parseInt(text))
                        , "Pimple rating is updated");
            }
            default -> {
                log.error("Unknown user state " + userState);
                return "Unknown error! Enter /cancel and try again!";
            }
        }
    }

    private String setDayRatingToday(BotUser botUser, Consumer<Day> dayConsumer, String response) {
        var dayOptional = dayRepository.findByDate(LocalDate.now());
        var day = dayOptional.orElseThrow(() -> {
            log.error("Today is not present after day record was started");
            return new DatabaseException("Can't find today");
        });
        dayConsumer.accept(day);
        dayRepository.save(day);
        botUser.setUserState(BASIC_STATE);
        botUserRepository.save(botUser);
        log.info("User " + botUser.getName()
                + " called: setDayRating"
                + " new state is: " + botUser.getUserState());
        return response;
    }

    private String processServiceCommand(BotUser botUser, String command) {
        try {
            var serviceCommand = ServiceCommands.getServiceCommandByValue(command);
            switch (Objects.requireNonNull(serviceCommand)) {
                case HELP -> {
                    return help(botUser);
                }
                case START -> {
                    return "Hello there, " + botUser.getName() + "!"
                            + " Enter /help to see available commands";
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
                        log.info("User " + botUser.getName()
                                + " called: " + DAY.getCommand()
                                + " new state is: " + botUser.getUserState());
                        return "You have started this day's record";
                    }
                }
                case FOOD_RECORD -> {
                    if (dayRepository.existsDayByDateIs(LocalDate.now())) {
                        botUser.setUserState(WAIT_FOR_INPUT_FOOD);
                        botUserRepository.save(botUser);
                        log.info("User " + botUser.getName()
                                + " called: " + FOOD_RECORD.getCommand()
                                + " new state is: " + botUser.getUserState());
                        return "Enter food";
                    } else {
                        var day = Day.builder()
                                .date(LocalDate.now())
                                .creator(botUser)
                                .build();
                        dayRepository.save(day);
                        botUser.setUserState(WAIT_FOR_INPUT_FOOD);
                        botUserRepository.save(botUser);
                        log.info("User " + botUser.getName()
                                + " called: " + FOOD_RECORD.getCommand()
                                + " new state is: " + botUser.getUserState());
                        return "You have started this day's record. Enter food";
                    }
                }
                case IS_BLOOD -> {
                    return askForRatingAndChangeState(botUser, WAIT_FOR_INPUT_BLOOD,
                            "How bloody is the poop? From 0 to 10");
                }
                case IS_PIMPLE -> {
                    return askForRatingAndChangeState(botUser, WAIT_FOR_INPUT_PIMPLE,
                            "How much pimples? From 0 to 10");
                }
                case CANCEL -> {
                    return cancel(botUser);
                }
            }
            return unknown(command);
        } catch (RuntimeException e) {
            return unknown(command);
        }
    }

    private String askForRatingAndChangeState(BotUser botUser, UserState state, String message) {
        var dayOptional = dayRepository.findByDate(LocalDate.now());
        if (dayOptional.isPresent()) {
            botUser.setUserState(state);
            botUserRepository.save(botUser);
            return message;
        } else {
            return "You haven't started today's record!";
        }
    }

    private String help(BotUser botUser) {
        log.info("User " + botUser.getName()
                + " called: " + HELP.getCommand()
                + " state is: " + botUser.getUserState());
        return """
                List of available commands:\s
                /cancel - cancel current command;
                /day - start today's record;
                /food - add food record;
                /blood - changes the day to bloody;
                /pimple - changes the day to pimple""";
    }

    private String unknown(String command) {
        log.error("Unknown command: " + command);
        return "Unknown command! Enter /help to see available commands";
    }

    private String cancel(BotUser botUser) {
        botUser.setUserState(BASIC_STATE);
        botUserRepository.save(botUser);
        log.info("User " + botUser.getName()
                + " called: " + CANCEL.getCommand()
                + " state is: " + botUser.getUserState());
        return "Command canceled!";
    }
}
