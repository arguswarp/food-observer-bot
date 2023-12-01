package com.argus.foodobserverbot.telegram.handler.impl;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.exception.DatabaseException;
import com.argus.foodobserverbot.repository.BotUserRepository;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.repository.FoodRecordRepository;
import com.argus.foodobserverbot.telegram.enums.ServiceCommands;
import com.argus.foodobserverbot.telegram.handler.CommandProcessor;
import com.argus.foodobserverbot.telegram.handler.UpdateHandler;
import com.argus.foodobserverbot.utils.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import static com.argus.foodobserverbot.entity.enums.UserState.BASIC_STATE;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.CANCEL;

@Component
@Log4j2
public class MessageHandler implements UpdateHandler<Message> {

    private final BotUserRepository botUserRepository;

    private final DayRepository dayRepository;

    private final FoodRecordRepository foodRecordRepository;

    private final CommandProcessor commandProcessor;

    private final MessageUtils messageUtils;

    public MessageHandler(BotUserRepository botUserRepository, DayRepository dayRepository, FoodRecordRepository foodRecordRepository, CommandProcessor commandProcessor, MessageUtils messageUtils) {
        this.botUserRepository = botUserRepository;
        this.dayRepository = dayRepository;
        this.foodRecordRepository = foodRecordRepository;
        this.commandProcessor = commandProcessor;
        this.messageUtils = messageUtils;
    }

    @Override
    public SendMessage handleUpdate(Message message, BotUser botUser) {
        var userState = botUser.getUserState();
        String text = message.getText();

        if (ServiceCommands.isCommand(text) && (text.equals(CANCEL.getCommand()) || userState.equals(BASIC_STATE))) {
            return commandProcessor.process(message, botUser);
        }

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
}
