package com.argus.foodobserverbot.telegram.handler.impl;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.telegram.handler.CommandProcessor;
import com.argus.foodobserverbot.telegram.handler.UpdateHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@Log4j2
public class CallbackQueryHandler implements UpdateHandler<CallbackQuery> {

    private final CommandProcessor commandProcessor;

    public CallbackQueryHandler(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    @Override
    public PartialBotApiMethod<?> handleUpdate(CallbackQuery callbackQuery, BotUser botUser) {
        return commandProcessor.process(callbackQuery, botUser);
    }
}
