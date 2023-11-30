package com.argus.foodobserverbot.controller;


import com.argus.foodobserverbot.service.BotUserService;
import com.argus.foodobserverbot.telegram.handler.impl.CallbackQueryHandler;
import com.argus.foodobserverbot.telegram.handler.impl.MessageHandler;
import com.argus.foodobserverbot.utils.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
@Log4j2
public class UpdateController {
    private final MessageUtils messageUtils;
    private final CallbackQueryHandler callbackQueryHandler;
    private final MessageHandler messageHandler;
    private final BotUserService botUserService;

    public UpdateController(MessageUtils messageUtils, CallbackQueryHandler callbackQueryHandler, MessageHandler messageHandler, BotUserService botUserService) {
        this.messageUtils = messageUtils;
        this.callbackQueryHandler = callbackQueryHandler;
        this.messageHandler = messageHandler;
        this.botUserService = botUserService;
    }

    public SendMessage processUpdate(Update update) {
        if (update == null) {
            log.error("Received update is null");
            throw new IllegalArgumentException("Update is null");
        }
        var botUser = botUserService.findOrSaveAppUser(update);
        if (update.hasCallbackQuery()) {
            return callbackQueryHandler.handleUpdate(update.getCallbackQuery(), botUser);
        } else if (update.hasMessage()) {
            return messageHandler.handleUpdate(update.getMessage(), botUser);
        } else {
            log.error("Received unsupported message type " + update);
            return messageUtils.generateSendMessageWithText(update,
                    "Unsupported message type");
        }
    }
}
