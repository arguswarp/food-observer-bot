package com.argus.foodobserverbot.telegram.handler;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.enums.UserState;
import com.argus.foodobserverbot.exception.UnknownServiceCommandException;
import com.argus.foodobserverbot.repository.BotUserRepository;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.service.ExcelService;
import com.argus.foodobserverbot.service.MenuService;
import com.argus.foodobserverbot.telegram.enums.ServiceCommands;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static com.argus.foodobserverbot.entity.enums.UserState.*;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.*;

@Component
@Log4j2
public class CommandProcessor {
    private final BotUserRepository botUserRepository;
    private final DayRepository dayRepository;
    private final MenuService menuService;
    private final ExcelService excelService;
    @Value("${excel.path}")
    private String EXCEL_PATH;

    public CommandProcessor(BotUserRepository botUserRepository, DayRepository dayRepository, MenuService menuService, ExcelService excelService) {
        this.botUserRepository = botUserRepository;
        this.dayRepository = dayRepository;
        this.menuService = menuService;
        this.excelService = excelService;
    }

    public PartialBotApiMethod<?> process(Message message, BotUser botUser) {
        var chatId = message.getChatId();
        var text = message.getText();
        return processServiceCommand(botUser, chatId, text);
    }

    public PartialBotApiMethod<?> process(CallbackQuery callbackQuery, BotUser botUser) {
        var chatId = callbackQuery.getMessage().getChatId();
        var text = callbackQuery.getData();
        return processServiceCommand(botUser, chatId, text);
    }

    private PartialBotApiMethod<?> processServiceCommand(BotUser botUser, Long chatId, String text) {
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
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of("Add food record", "Add pimples","Get excel all data", "Help"),
                                    List.of(FOOD_RECORD.getCommand(), IS_PIMPLE.getCommand(),EXCEL_ALL_DATA.getCommand(), HELP.getCommand())))
                            .build();
                }
                case DAY -> {
                    if (!dayRepository.existsDayByDateIs(LocalDate.now())) {
                        var day = Day.builder()
                                .date(LocalDate.now())
                                .creator(botUser)
                                .build();
                        dayRepository.save(day);
                    }
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("You have started this day's record")
                            .build();
                }
                case FOOD_RECORD -> {
                    if (!dayRepository.existsDayByDateIs(LocalDate.now())) {
                        var day = Day.builder()
                                .date(LocalDate.now())
                                .creator(botUser)
                                .build();
                        dayRepository.save(day);
                    }
                    botUser.setUserState(INPUT_FOOD);
                    botUserRepository.save(botUser);
                    log.info("User " + botUser.getName()
                            + " called: " + FOOD_RECORD.getCommand()
                            + " new state is: " + botUser.getUserState());
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Enter food")
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of("Cancel"),
                                    List.of(CANCEL.getCommand())))
                            .build();
                }
                case EXCEL_ALL_DATA -> {
                    return SendDocument.builder()
                            .chatId(chatId)
                            .document(new InputFile(excelService.createExcelFileAllData(EXCEL_PATH, botUser)))
                            .caption("Your file is ready " + botUser.getName())
                            .build();
                }
                case IS_BLOOD -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(askAndChangeState(botUser,
                                    INPUT_BLOOD_RATE,
                                    "How bloody is the poop? From 0 to 10"))
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of("Cancel"),
                                    List.of(CANCEL.getCommand())))
                            .build();
                }
                case IS_PIMPLE -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(askAndChangeState(botUser,
                                    BASIC_STATE,
                                    "Choose where the pimples are: "
                                            + PIMPLE_FACE.getCommand()
                                            + " or "
                                            + PIMPLE_BOOTY.getCommand()))
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of("Face", "Booty"),
                                    List.of(PIMPLE_FACE.getCommand(), PIMPLE_BOOTY.getCommand())))
                            .build();
                }
                case PIMPLE_FACE -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(askAndChangeState(botUser,
                                    INPUT_PIMPLE_RATE_FACE,
                                    "How much pimples? From 0 to 10"))
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of("Cancel"),
                                    List.of(CANCEL.getCommand())))
                            .build();
                }
                case PIMPLE_BOOTY -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(askAndChangeState(botUser,
                                    INPUT_PIMPLE_RATE_BOOTY,
                                    "How much pimples? From 0 to 10"))
                            .replyMarkup(menuService.createOneRowReplyKeyboard(
                                    List.of("Cancel"),
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

    private String askAndChangeState(BotUser botUser, UserState state, String message) {
        if (!dayRepository.existsDayByDateIs(LocalDate.now())) {
            var day = Day.builder()
                    .date(LocalDate.now())
                    .creator(botUser)
                    .build();
            dayRepository.save(day);
        }
        botUser.setUserState(state);
        botUserRepository.save(botUser);
        return message;
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
        return "Command canceled";
    }
}
