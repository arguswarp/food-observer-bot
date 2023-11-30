package com.argus.foodobserverbot.telegram.handler.impl;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.repository.BotUserRepository;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.telegram.enums.ServiceCommands;
import com.argus.foodobserverbot.telegram.handler.UpdateHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.LocalDate;

import static com.argus.foodobserverbot.entity.enums.UserState.INPUT_FOOD;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.DAY;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.FOOD_RECORD;

@Component
@Log4j2
public class CallbackQueryHandler implements UpdateHandler<CallbackQuery> {

    private final DayRepository dayRepository;

    private final BotUserRepository botUserRepository;

    public CallbackQueryHandler(DayRepository dayRepository, BotUserRepository botUserRepository) {
        this.dayRepository = dayRepository;
        this.botUserRepository = botUserRepository;
    }

    @Override
    public SendMessage handleUpdate(CallbackQuery callbackQuery, BotUser botUser) {
        var chatId = callbackQuery.getMessage().getChatId();
        var callbackData = callbackQuery.getData();
        //TODO: use service class after refactor
        var serviceCommand = ServiceCommands.getServiceCommandByValue(callbackData);
        switch (serviceCommand) {
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

        }
        return null;
    }
}
