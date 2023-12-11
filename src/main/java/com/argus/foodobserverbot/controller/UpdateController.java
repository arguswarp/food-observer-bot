package com.argus.foodobserverbot.controller;

import com.argus.foodobserverbot.exception.EmptyUpdateException;
import com.argus.foodobserverbot.service.BotUserService;
import com.argus.foodobserverbot.telegram.handler.UpdateHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Log4j2
public class UpdateController {
    private final UpdateHandler<CallbackQuery> callbackQueryHandler;
    private final UpdateHandler<Message> messageHandler;
    private final BotUserService botUserService;

    public UpdateController(UpdateHandler<CallbackQuery> callbackQueryHandler, UpdateHandler<Message> messageHandler, BotUserService botUserService) {
        this.callbackQueryHandler = callbackQueryHandler;
        this.messageHandler = messageHandler;
        this.botUserService = botUserService;
    }

    public PartialBotApiMethod<?> processUpdate(Update update) {
        if (update == null) {
            log.error("Received update is null");
            throw new EmptyUpdateException("Update is null");
        }
        var botUser = botUserService.findOrSaveAppUser(update);
        if (update.hasCallbackQuery()) {
            return callbackQueryHandler.handleUpdate(update.getCallbackQuery(), botUser);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            return messageHandler.handleUpdate(update.getMessage(), botUser);
        } else {
            log.error("Received unsupported message type " + update);
            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Unsupported message type")
                    .build();
        }
    }
}
