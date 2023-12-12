package com.argus.foodobserverbot.telegram.handler;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.service.BotUserService;
import com.argus.foodobserverbot.service.DayService;
import com.argus.foodobserverbot.service.FoodRecordService;
import com.argus.foodobserverbot.service.MenuService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

import static com.argus.foodobserverbot.entity.enums.UserState.BASIC_STATE;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.*;

@Component
@Log4j2
public class UserStateProcessor {
    private final DayService dayService;
    private final BotUserService botUserService;
    private final FoodRecordService foodRecordService;
    private final MenuService menuService;

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
                var food = foodRecordService.addFood(text, botUserService.selectDate(botUser));
                var user = botUserService.changeState(botUser, BASIC_STATE);
                log.info("User " + user.getName()
                        + " new food input " + food.getFood());
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("You added food record")
                        .replyMarkup(menuService.createOneRowReplyKeyboard(
                                List.of("Another food", "Another record", "Cancel"),
                                List.of(FOOD_RECORD.getCommand(), RECORD.getCommand(), CANCEL.getCommand())
                        ))
                        .build();
            }
            case INPUT_BLOOD_RATE -> {
                var dayUpdated = dayService.setDayRating(botUserService.selectDate(botUser),
                        day -> day.setBloodyRating(Integer.parseInt(text)));
                var user = botUserService.changeState(botUser, BASIC_STATE);
                log.info("Bloody rating is updated by " + user.getName()
                        + " new value is " + dayUpdated.getBloodyRating());
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Bloody rating is updated")
                        .build();
            }
            case INPUT_PIMPLE_RATE_FACE -> {
                var dayUpdated = dayService.setDayRating(botUserService.selectDate(botUser),
                        day -> day.setPimpleFaceRating(Integer.parseInt(text)));
                var user = botUserService.changeState(botUser, BASIC_STATE);
                log.info("Pimple face rating is updated by " + user.getName()
                        + " new value is " + dayUpdated.getPimpleFaceRating());
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Pimple face rating is updated")
                        .build();
            }
            case INPUT_PIMPLE_RATE_BOOTY -> {
                var dayUpdated = dayService.setDayRating(botUserService.selectDate(botUser),
                        day -> day.setPimpleBootyRating(Integer.parseInt(text)));
                var user = botUserService.changeState(botUser, BASIC_STATE);
                log.info("Pimple booty rating is updated by " + user.getName()
                        + " new value is " + dayUpdated.getPimpleBootyRating());
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Pimple booty rating is updated")
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
}
