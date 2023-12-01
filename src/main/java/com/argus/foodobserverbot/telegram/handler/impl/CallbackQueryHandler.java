package com.argus.foodobserverbot.telegram.handler.impl;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.repository.BotUserRepository;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.telegram.enums.ServiceCommands;
import com.argus.foodobserverbot.telegram.handler.CommandProcessor;
import com.argus.foodobserverbot.telegram.handler.UpdateHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@Log4j2
public class CallbackQueryHandler implements UpdateHandler<CallbackQuery> {

    private final DayRepository dayRepository;

    private final BotUserRepository botUserRepository;

    private final CommandProcessor commandProcessor;

    public CallbackQueryHandler(DayRepository dayRepository, BotUserRepository botUserRepository, CommandProcessor commandProcessor) {
        this.dayRepository = dayRepository;
        this.botUserRepository = botUserRepository;
        this.commandProcessor = commandProcessor;
    }

    @Override
    public SendMessage handleUpdate(CallbackQuery callbackQuery, BotUser botUser) {
        return commandProcessor.process(callbackQuery, botUser);
    }
}
