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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static com.argus.foodobserverbot.entity.enums.UserRole.ADMIN;
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
                case START -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Hello there, " + botUser.getName() + "!"
                                  + " Enter /help to see available commands")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(MENU, HELP))
                            .build();
                }
                case HELP -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(help(botUser))
                            .build();
                }
                case MENU -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Choose the record type to add")
                            .replyMarkup(menuService.createTwoRowReplyKeyboard(
                                    List.of(FOOD_RECORD, IS_BLOOD, IS_PIMPLE, NOTE),
                                    List.of(SHOW, SHOW_NOTES, EXCEL_USER_DATA)
                            ))
                            .build();
                }
                case NOTE -> {
                    changeState(botUser, INPUT_NOTE, NOTE);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Enter note for "+ (botUser.getTodayMode() ? "today" : "yesterday"))
                            .replyMarkup(menuService.createOneRowReplyKeyboard(MODE, CANCEL))
                            .build();
                }
                case SHOW -> {
                    var foodRecords = botUserService.getTodayFoodRecords(botUser);
                    var foodRecordsText = foodRecords.stream()
                            .map(foodRecord -> foodRecord.getCreatedAt()
                                                       .format(DateTimeFormatter.ofPattern("HH:mm")) + " " + foodRecord.getFood())
                            .reduce((s1, s2) -> s1 + "\n" + s2)
                            .orElse("No records added yet");
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(foodRecordsText)
                            .replyMarkup(menuService.createTwoRowReplyKeyboard(
                                    List.of(FOOD_RECORD, IS_BLOOD, IS_PIMPLE, NOTE),
                                    List.of(SHOW, SHOW_NOTES, EXCEL_USER_DATA)
                            ))
                            .build();
                }
                case SHOW_NOTES -> {
                    var notes = botUserService.getTodayNotes(botUser);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(notes.isBlank() ? "No notes added yet" : notes)
                            .replyMarkup(menuService.createTwoRowReplyKeyboard(
                                    List.of(FOOD_RECORD, IS_BLOOD, IS_PIMPLE, NOTE),
                                    List.of(SHOW, SHOW_NOTES, EXCEL_USER_DATA)
                            ))
                            .build();
                }
                case MODE -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Choose the day for record")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(DAY_TODAY, DAY_YESTERDAY, CANCEL))
                            .build();
                }
                case DAY_TODAY -> {
                    var user = botUserService.changeTodayMode(botUser, true);
                    var state = botUser.getUserState();
                    String commandText = "";

                    if (state.equals(INPUT_FOOD)) {
                        commandText = "Enter food. ";
                    }
                    if (state.equals(INPUT_NOTE)) {
                        commandText = "Enter note. ";
                    }

                    if (state.equals(INPUT_BLOOD_RATE)) {
                        commandText = "How bloody? From 0 to 10. ";
                    }

                    if (state.equals(INPUT_PIMPLE_RATE_FACE)) {
                        commandText = "How much face pimples? From 0 to 10. ";
                    }

                    getInfo(user.getName(), DAY_TODAY.getCommand(), user.getUserState());
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(commandText + "You now saving today records")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(CANCEL))
                            .build();
                }
                case DAY_YESTERDAY -> {
                    var user = botUserService.changeTodayMode(botUser, false);
                    var state = botUser.getUserState();
                    String commandText = "";

                    if (state.equals(INPUT_FOOD)) {
                        commandText = "Enter food. ";
                    }
                    if (state.equals(INPUT_NOTE)) {
                        commandText = "Enter note. ";
                    }

                    if (state.equals(INPUT_BLOOD_RATE)) {
                        commandText = "How bloody? From 0 to 10. ";
                    }

                    if (state.equals(INPUT_PIMPLE_RATE_FACE)) {
                        commandText = "How much face pimples? From 0 to 10. ";
                    }

                    getInfo(user.getName(), DAY_YESTERDAY.getCommand(), user.getUserState());
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(commandText + "You now saving yesterday records")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(CANCEL))
                            .build();
                }
                case FOOD_RECORD -> {
                    changeState(botUser, INPUT_FOOD, FOOD_RECORD);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Enter food for " + (botUser.getTodayMode() ? "today" : "yesterday"))
                            .replyMarkup(menuService.createOneRowReplyKeyboard(MODE, CANCEL))
                            .build();
                }
                case EXCEL_ALL_DATA -> {
                    if (botUser.getUserRole() != ADMIN) {
                        return SendMessage.builder()
                                .chatId(chatId)
                                .text("You are not allowed to access this resource.")
                                .replyMarkup(menuService.createOneRowReplyKeyboard(MENU))
                                .build();
                    }
                    return SendDocument.builder()
                            .chatId(chatId)
                            .document(new InputFile(excelService.createExcelAllRecords(EXCEL_PATH, botUser)))
                            .caption("Your file is ready " + botUser.getName())
                            .replyMarkup(menuService.createTwoRowReplyKeyboard(
                                    List.of(FOOD_RECORD, IS_BLOOD, IS_PIMPLE, NOTE),
                                    List.of(SHOW, SHOW_NOTES, EXCEL_USER_DATA)
                            ))
                            .build();
                }
                case EXCEL_USER_DATA -> {
                    return SendDocument.builder()
                            .chatId(chatId)
                            .document(new InputFile(excelService.createExcelUserRecords(EXCEL_PATH, botUser)))
                            .caption("Your file is ready " + botUser.getName())
                            .replyMarkup(menuService.createTwoRowReplyKeyboard(
                                    List.of(FOOD_RECORD, IS_BLOOD, IS_PIMPLE, NOTE),
                                    List.of(SHOW, SHOW_NOTES, EXCEL_USER_DATA)
                            ))
                            .build();
                }
                case IS_BLOOD -> {
                    changeState(botUser, INPUT_BLOOD_RATE, IS_BLOOD);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("How bloody is the poop? From 0 to 10")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(MODE, CANCEL))
                            .build();
                }
                case IS_PIMPLE -> {
                    changeState(botUser, BASIC_STATE, IS_PIMPLE);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Choose where the pimples "
                                  + (botUser.getTodayMode() ? "are today" : "were yesterday"))
                            .replyMarkup(menuService.createOneRowReplyKeyboard(PIMPLE_FACE, PIMPLE_BOOTY, CANCEL))
                            .build();
                }
                case PIMPLE_FACE -> {
                    changeState(botUser, INPUT_PIMPLE_RATE_FACE, PIMPLE_FACE);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("How much pimples? From 0 to 10")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(MODE, CANCEL))
                            .build();
                }
                case PIMPLE_BOOTY -> {
                    changeState(botUser, INPUT_PIMPLE_RATE_BOOTY, PIMPLE_BOOTY);
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("How much pimples? From 0 to 10")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(MODE, CANCEL))
                            .build();
                }
                case CANCEL -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(cancel(botUser))
                            .replyMarkup(menuService.createOneRowReplyKeyboard(MENU))
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

    private void changeState(BotUser botUser, UserState state, ServiceCommands command) {
        var day = dayService.findOrSaveDay(botUser, botUserService.selectDate(botUser));
        var user = botUserService.changeState(botUser, state);
        log.info("User {} called {} on day {}", user.getName(), command.getCommand(), day.getDate());
    }

    private String help(BotUser botUser) {
        getInfo(botUser.getName(), HELP.getCommand(), botUser.getUserState());
        return """
                List of available commands:\s
                /start - start the bot
                /menu - main menu to add records
                /food - add food record
                /blood - set rating for blood in the poop
                /pimple - set rating for pimples
                /note - add note to day record
                /mode - change record mode from today to yesterday
                /excel - save your records to excel file""";
    }

    private void getInfo(String name, String command, UserState state) {
        log.info("User {} called: {}, state is: {}", name, command, state);
    }

    private String unknown(String command) {
        log.error("Unknown command: {}", command);
        return "Unknown command! Enter /help to see available commands";
    }

    private String cancel(BotUser botUser) {
        botUserService.changeState(botUser, BASIC_STATE);
        getInfo(botUser.getName(), CANCEL.getCommand(), botUser.getUserState());
        return "Command canceled";
    }
}
