package com.argus.foodobserverbot.telegram.handler;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.service.BotUserService;
import com.argus.foodobserverbot.service.DayService;
import com.argus.foodobserverbot.service.FoodRecordService;
import com.argus.foodobserverbot.service.MenuService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.function.BiConsumer;

import static com.argus.foodobserverbot.entity.enums.UserState.BASIC_STATE;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.*;

@Component
@Log4j2
public class UserStateProcessor {
    private final DayService dayService;
    private final BotUserService botUserService;
    private final FoodRecordService foodRecordService;
    private final MenuService menuService;
    private static final String NUMBER_PATTERN = "^(10|[0-9])$";

    public UserStateProcessor(DayService dayService, BotUserService botUserService, FoodRecordService foodRecordService, MenuService menuService) {
        this.dayService = dayService;
        this.botUserService = botUserService;
        this.foodRecordService = foodRecordService;
        this.menuService = menuService;
    }

    public SendMessage process(BotUser botUser, Long chatId, String text) {
        var userState = botUser.getUserState();
        switch (userState) {
            case INPUT_FOOD -> {
                var food = foodRecordService.addFood(text, botUserService.selectDate(botUser), botUser);
                var user = botUserService.changeState(botUser, BASIC_STATE);
                botUserService.changeTodayMode(botUser, true);
                log.info("User {} new food input {}", user.getName(), food.getFood());
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("You added food record. Mode is set to today.")
                        .replyMarkup(menuService.createTwoRowReplyKeyboard(
                                List.of(FOOD_RECORD, IS_BLOOD, IS_PIMPLE, NOTE),
                                List.of(SHOW, SHOW_NOTES, EXCEL_USER_DATA)
                        ))
                        .build();
            }
            case INPUT_NOTE -> {
                var day = dayService.addNote(text, botUserService.selectDate(botUser), botUser);
                var user = botUserService.changeState(botUser, BASIC_STATE);
                log.info("User {} new note: {}; on day {}", user.getName(), text, day.getDate());
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("You added note")
                        .replyMarkup(menuService.createTwoRowReplyKeyboard(
                                List.of(FOOD_RECORD, IS_BLOOD, IS_PIMPLE, NOTE),
                                List.of(SHOW, SHOW_NOTES, EXCEL_USER_DATA)
                        ))
                        .build();
            }
            case INPUT_BLOOD_RATE -> {
                return processInputRating(botUser, text, chatId, (input, day) -> day.setBloodyRating(input),
                        "Pimple blood rating is updated");
            }
            case INPUT_PIMPLE_RATE_FACE -> {
                return processInputRating(botUser, text, chatId, (input, day) -> day.setPimpleFaceRating(input),
                        "Pimple face rating is updated");
            }
            case INPUT_PIMPLE_RATE_BOOTY -> {
                return processInputRating(botUser, text, chatId, (input, day) -> day.setPimpleBootyRating(input),
                        "Pimple booty rating is updated");
            }
            default -> {
                log.error("Unknown user state {}", userState);
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Unknown error! Enter /cancel and try again!")
                        .replyMarkup(menuService.createOneRowReplyKeyboard(CANCEL))
                        .build();
            }
        }
    }

    private SendMessage processInputRating(BotUser botUser, String text, Long chatId,
                                           BiConsumer<Integer, Day> consumer, String responseMessage) {
        if (text.matches(NUMBER_PATTERN)) {
            final var userState = botUser.getUserState();
            int rating = Integer.parseInt(text);
            var date = botUserService.selectDate(botUser);
            dayService.setDayRating(rating, date, botUser, consumer);
            var user = botUserService.changeState(botUser, BASIC_STATE);
            log.info("{} day rating is updated by {} new value is {}", userState, user.getName(), rating);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(responseMessage)
                    .replyMarkup(menuService.createTwoRowReplyKeyboard(
                            List.of(FOOD_RECORD, IS_BLOOD, IS_PIMPLE, NOTE),
                            List.of(SHOW, SHOW_NOTES, EXCEL_USER_DATA)
                    ))
                    .build();
        } else {
            log.error("Invalid input {} by {}: {}", botUser.getUserState(), botUser.getName(), text);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Input is incorrect. Rating must be the number between 0 and 10. Please try again or /cancel")
                    .build();
        }
    }
}
