package com.argus.foodobserverbot.telegram.handler.impl;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.telegram.handler.CommandProcessor;
import com.argus.foodobserverbot.telegram.handler.UpdateHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class CallbackQueryHandler implements UpdateHandler<CallbackQuery> {
    private final CommandProcessor commandProcessor;

    public CallbackQueryHandler(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    @Override
    public PartialBotApiMethod<?> handleUpdate(CallbackQuery callbackQuery, BotUser botUser) {
        var chatId = callbackQuery.getMessage().getChatId();
        var text = callbackQuery.getData();
        return commandProcessor.process(botUser, chatId, text);
    }
}
