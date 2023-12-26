package com.argus.foodobserverbot.telegram.handler;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.enums.UserState;
import com.argus.foodobserverbot.exception.UnknownServiceCommandException;
import com.argus.foodobserverbot.service.BotUserService;
import com.argus.foodobserverbot.service.DayService;
import com.argus.foodobserverbot.service.ExcelService;
import com.argus.foodobserverbot.service.MenuService;
import com.argus.foodobserverbot.telegram.enums.ServiceCommands;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.util.List;
import java.util.Objects;

import static com.argus.foodobserverbot.entity.enums.UserState.*;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.*;

@Component
@Log4j2
public class CommandProcessor {
    private final BotUserService botUserService;
    private final DayService dayService;
    private final MenuService menuService;
    private final ExcelService excelService;
    @Value("${excel.path}")
    private String EXCEL_PATH;

    public CommandProcessor(BotUserService botUserService, DayService dayService, MenuService menuService, ExcelService excelService) {
        this.botUserService = botUserService;
        this.dayService = dayService;
        this.menuService = menuService;
        this.excelService = excelService;
    }

    public PartialBotApiMethod<?> process(BotUser botUser, Long chatId, String text) {
        try {
            var serviceCommand = ServiceCommands.getServiceCommandByValue(text);
            switch (Objects.requireNonNull(serviceCommand)) {
                case HELP -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(help(botUser))
                            .build();
                }
                case START -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Hello there, " + botUser.getName() + "!"
                                    + " Enter /help to see available commands")
                            .replyMarkup(menuService.createMainMenu())
                            .build();
                }
                case RECORD -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Choose the record type to add")
                            .replyMarkup(menuService.createTwoRowReplyKeyboard(
                                    List.of(FOOD_RECORD.getButtonName(), IS_BLOOD.getButtonName(), IS_PIMPLE.getButtonName(), CANCEL.getButtonName()),
                                    List.of(FOOD_RECORD.getCommand(), IS_BLOOD.getCommand(), IS_PIMPLE.getCommand(), CANCEL.getCommand()),
                                    List.of("Change mode", EXCEL_USER_DATA.getButtonName(), EXCEL_ALL_DATA.getButtonName()),
                                    List.of(MODE.getCommand(), EXCEL_USER_DATA.getCommand(), EXCEL_ALL_DATA.getCommand())
                            ))
                            .build();
                }
                case MODE -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Choose the day for record")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of(DAY_TODAY.getButtonName(), DAY_YESTERDAY.getButtonName()),
                                    List.of(DAY_TODAY.getCommand(), DAY_YESTERDAY.getCommand())))
                            .build();
                }
                case DAY_TODAY -> {
                    var user = botUserService.changeTodayMode(botUser, true);
                    log.info("User " + user.getName()
                            + " changed mode to " + DAY_TODAY.getCommand());
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("You now saving today records")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of(RECORD.getButtonName(), MODE.getButtonName()),
                                    List.of(RECORD.getCommand(), MODE.getCommand())))
                            .build();
                }
                case DAY_YESTERDAY -> {
                    var user = botUserService.changeTodayMode(botUser, false);
                    log.info("User " + user.getName()
                            + " changed mode to " + DAY_YESTERDAY.getCommand());
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("You now saving yesterday records")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of(RECORD.getButtonName(), MODE.getButtonName()),
                                    List.of(RECORD.getCommand(), MODE.getCommand())))
                            .build();
                }
                case FOOD_RECORD -> {
                    newRecord(botUser, INPUT_FOOD, FOOD_RECORD);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Enter food for " + (botUser.getTodayMode() ? "today" : "yesterday"))
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of(MODE.getButtonName(), CANCEL.getButtonName()),
                                    List.of(MODE.getCommand(), CANCEL.getCommand())))
                            .build();
                }
                case EXCEL_ALL_DATA -> {
                    return SendDocument.builder()
                            .chatId(chatId)
                            .document(new InputFile(excelService.createExcelAllRecords(EXCEL_PATH, botUser)))
                            .caption("Your file is ready " + botUser.getName())
                            .build();
                }
                case EXCEL_USER_DATA -> {
                    return SendDocument.builder()
                            .chatId(chatId)
                            .document(new InputFile(excelService.createExcelUserRecords(EXCEL_PATH, botUser)))
                            .caption("Your file is ready " + botUser.getName())
                            .build();
                }
                case IS_BLOOD -> {
                    newRecord(botUser, INPUT_BLOOD_RATE, IS_BLOOD);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("How bloody is the poop? From 0 to 10")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of(CANCEL.getButtonName()),
                                    List.of(CANCEL.getCommand())))
                            .build();
                }
                case IS_PIMPLE -> {
                    newRecord(botUser, BASIC_STATE, IS_PIMPLE);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Choose where the pimples "
                                    + (botUser.getTodayMode() ? "are today" : "were yesterday"))
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of(PIMPLE_FACE.getButtonName(), PIMPLE_BOOTY.getButtonName(), CANCEL.getButtonName()),
                                    List.of(PIMPLE_FACE.getCommand(), PIMPLE_BOOTY.getCommand(), CANCEL.getCommand())))
                            .build();
                }
                case PIMPLE_FACE -> {
                    newRecord(botUser, INPUT_PIMPLE_RATE_FACE, PIMPLE_FACE);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("How much pimples? From 0 to 10")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of(CANCEL.getButtonName()),
                                    List.of(CANCEL.getCommand())))
                            .build();
                }
                case PIMPLE_BOOTY -> {
                    newRecord(botUser, INPUT_PIMPLE_RATE_BOOTY, PIMPLE_BOOTY);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("How much pimples? From 0 to 10")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of(CANCEL.getButtonName()),
                                    List.of(CANCEL.getCommand())))
                            .build();
                }
                case CANCEL -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(cancel(botUser))
                            .build();
                }
            }
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(unknown(text))
                    .build();
        } catch (UnknownServiceCommandException e) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(unknown(text))
                    .build();
        }
    }

    private void newRecord(BotUser botUser, UserState state, ServiceCommands command) {
        var day = dayService.findOrSaveDay(botUser, botUserService.selectDate(botUser));
        var user = botUserService.changeState(botUser, state);
        log.info("User " + user.getName()
                + " called " + command.getCommand()
                + " on day " + day.getDate());
    }

    private String help(BotUser botUser) {
        log.info("User " + botUser.getName()
                + " called: " + HELP.getCommand()
                + " state is: " + botUser.getUserState());
        return """
                List of available commands:\s
                /start - start the bot
                /record - main menu to add records
                /food - add food record;
                /blood - set rating for blood in the poop;
                /pimple - set rating for pimples
                /mode - change record mode from today to yesterday
                /excelall - save all records to excel file""";
    }

    private String unknown(String command) {
        log.error("Unknown command: " + command);
        return "Unknown command! Enter /help to see available commands";
    }

    private String cancel(BotUser botUser) {
        botUserService.changeState(botUser, BASIC_STATE);
        log.info("User " + botUser.getName()
                + " called: " + CANCEL.getCommand()
                + " state is: " + botUser.getUserState());
        return "Command canceled";
    }
}
