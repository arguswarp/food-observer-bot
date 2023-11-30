package com.argus.foodobserverbot.telegram.handler.impl;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.entity.enums.UserState;
import com.argus.foodobserverbot.exception.DatabaseException;
import com.argus.foodobserverbot.exception.UnknownServiceCommandException;
import com.argus.foodobserverbot.repository.BotUserRepository;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.repository.FoodRecordRepository;
import com.argus.foodobserverbot.telegram.enums.ServiceCommands;
import com.argus.foodobserverbot.telegram.handler.UpdateHandler;
import com.argus.foodobserverbot.utils.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.argus.foodobserverbot.entity.enums.UserState.*;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.*;

@Component
@Log4j2
public class MessageHandler implements UpdateHandler<Message> {

    private final BotUserRepository botUserRepository;

    private final DayRepository dayRepository;

    private final FoodRecordRepository foodRecordRepository;

    private final MessageUtils messageUtils;

    public MessageHandler(BotUserRepository botUserRepository, DayRepository dayRepository, FoodRecordRepository foodRecordRepository, MessageUtils messageUtils) {
        this.botUserRepository = botUserRepository;
        this.dayRepository = dayRepository;
        this.foodRecordRepository = foodRecordRepository;
        this.messageUtils = messageUtils;
    }

    @Override
    public SendMessage handleUpdate(Message message, BotUser botUser) {
        var userState = botUser.getUserState();
        String text = message.getText();
        if (text.equals(CANCEL.getCommand())) {
            return messageUtils.generateSendMessageWithText(message, cancel(botUser));
        }
        switch (userState) {
            case BASIC_STATE -> {
                return processServiceCommand(message, botUser);
            }
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
                return messageUtils.generateSendMessageWithText(message, "You added food record");
            }
            case INPUT_BLOOD_RATE -> {
                return messageUtils.generateSendMessageWithText(message,
                        setDayRatingToday(botUser,
                                day -> day.setBloodyRating(Integer.parseInt(text)),
                                "Bloody rating is updated"));
            }
            case INPUT_PIMPLE_RATE_FACE -> {
                return messageUtils.generateSendMessageWithText(message,
                        setDayRatingToday(botUser,
                                day -> day.setPimpleFaceRating(Integer.parseInt(text)),
                                "Pimple rating is updated"));
            }
            case INPUT_PIMPLE_RATE_BOOTY -> {
                return messageUtils.generateSendMessageWithText(message,
                        setDayRatingToday(botUser,
                                day -> day.setPimpleBootyRating(Integer.parseInt(text)),
                                "Pimple rating is updated"));
            }
            default -> {
                log.error("Unknown user state " + userState);
                return messageUtils.generateSendMessageWithText(message,
                        "Unknown error! Enter /cancel and try again!");
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

    //TODO: move to separate service class
    private ReplyKeyboard replyKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton dayRecordButton = new InlineKeyboardButton();
        dayRecordButton.setText("Start day record");
        InlineKeyboardButton foodRecordButton = new InlineKeyboardButton();
        foodRecordButton.setText("New food record");


        dayRecordButton.setCallbackData(DAY.getCommand());
        foodRecordButton.setCallbackData(FOOD_RECORD.getCommand());


        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();

        keyboardButtonsRow.add(dayRecordButton);
        keyboardButtonsRow.add(foodRecordButton);


        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    //TODO: extract separate class
    private SendMessage processServiceCommand(Message message, BotUser botUser) {
        var chatId = message.getChatId();
        var text = message.getText();

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
                            .replyMarkup(replyKeyboard())
                            .build();
                }
                case DAY -> {
                    if (dayRepository.existsDayByDateIs(LocalDate.now())) {
                        return SendMessage.builder()
                                .chatId(chatId)
                                .text("You have already started this day's record")
                                .build();
                    } else {
                        var day = Day.builder()
                                .date(LocalDate.now())
                                .creator(botUser)
                                .build();
                        dayRepository.save(day);
                        log.info("User " + botUser.getName()
                                + " called: " + DAY.getCommand()
                                + " new state is: " + botUser.getUserState());
                        return SendMessage.builder()
                                .chatId(chatId)
                                .text("You have started this day's record")
                                .build();
                    }
                }
                case FOOD_RECORD -> {
                    if (dayRepository.existsDayByDateIs(LocalDate.now())) {
                        botUser.setUserState(INPUT_FOOD);
                        botUserRepository.save(botUser);
                        log.info("User " + botUser.getName()
                                + " called: " + FOOD_RECORD.getCommand()
                                + " new state is: " + botUser.getUserState());
                        return SendMessage.builder()
                                .chatId(chatId)
                                .text("Enter food")
                                .build();
                    } else {
                        var day = Day.builder()
                                .date(LocalDate.now())
                                .creator(botUser)
                                .build();
                        dayRepository.save(day);
                        botUser.setUserState(INPUT_FOOD);
                        botUserRepository.save(botUser);
                        log.info("User " + botUser.getName()
                                + " called: " + FOOD_RECORD.getCommand()
                                + " new state is: " + botUser.getUserState());
                        return SendMessage.builder()
                                .chatId(chatId)
                                .text("You have started this day's record. Enter food")
                                .build();
                    }
                }
                case IS_BLOOD -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(askAndChangeState(botUser,
                                    INPUT_BLOOD_RATE,
                                    "How bloody is the poop? From 0 to 10"))
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
                            .build();
                }
                case PIMPLE_FACE -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(askAndChangeState(botUser,
                                    INPUT_PIMPLE_RATE_FACE,
                                    "How much pimples? From 0 to 10"))
                            .build();
                }
                case PIMPLE_BOOTY -> {
                    return SendMessage.builder()
                            .chatId(chatId)
                            .text(askAndChangeState(botUser,
                                    INPUT_PIMPLE_RATE_BOOTY,
                                    "How much pimples? From 0 to 10"))
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
