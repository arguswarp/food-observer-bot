package com.argus.foodobserverbot.telegram.handler;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.exception.DatabaseException;
import com.argus.foodobserverbot.repository.BotUserRepository;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.repository.FoodRecordRepository;
import com.argus.foodobserverbot.service.MenuService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static com.argus.foodobserverbot.entity.enums.UserState.BASIC_STATE;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.CANCEL;

@Component
@Log4j2
public class UserStateProcessor {
    private final DayRepository dayRepository;

    private final BotUserRepository botUserRepository;

    private final FoodRecordRepository foodRecordRepository;

    private final MenuService menuService;

    public UserStateProcessor(DayRepository dayRepository, BotUserRepository botUserRepository, FoodRecordRepository foodRecordRepository, MenuService menuService) {
        this.dayRepository = dayRepository;
        this.botUserRepository = botUserRepository;
        this.foodRecordRepository = foodRecordRepository;
        this.menuService = menuService;
    }

    public SendMessage process(Message message, BotUser botUser) {
        var userState = botUser.getUserState();
        var text = message.getText();
        var chatId = message.getChatId();
        switch (userState) {
            case INPUT_FOOD -> {
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
                return SendMessage.builder()
                        .chatId(chatId)
                        .text(text)
                        .build();
            }
            case INPUT_BLOOD_RATE -> {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text(setDayRatingToday(botUser,
                                day -> day.setBloodyRating(Integer.parseInt(text)),
                                "Bloody rating is updated"))
                        .build();
            }
            case INPUT_PIMPLE_RATE_FACE -> {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text(setDayRatingToday(botUser,
                                day -> day.setPimpleFaceRating(Integer.parseInt(text)),
                                "Pimple rating is updated"))
                        .build();
            }
            case INPUT_PIMPLE_RATE_BOOTY -> {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text(setDayRatingToday(botUser,
                                day -> day.setPimpleBootyRating(Integer.parseInt(text)),
                                "Pimple rating is updated"))
                        .build();
            }
            default -> {
                log.error("Unknown user state " + userState);
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Unknown error! Enter /cancel and try again!")
                        .replyMarkup(menuService.createOneRowReplyKeyboard(
                                List.of("Cancel"),
                                List.of(CANCEL.getCommand())
                        ))
                        .build();
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
                + " state is: " + botUser.getUserState());
        return response;
    }
}
