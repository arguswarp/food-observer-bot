package com.argus.foodobserverbot.controller;


import com.argus.foodobserverbot.service.BotUserService;
import com.argus.foodobserverbot.telegram.handler.UpdateHandler;
import com.argus.foodobserverbot.utils.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
@Log4j2
public class UpdateController {
    private final MessageUtils messageUtils;
    private final UpdateHandler updateHandler;
    private final BotUserService botUserService;

    public UpdateController(MessageUtils messageUtils, UpdateHandler updateHandler, BotUserService botUserService) {
        this.messageUtils = messageUtils;
        this.updateHandler = updateHandler;
        this.botUserService = botUserService;
    }

    public SendMessage processUpdate(Update update) {
        if (update == null) {
            log.error("Receive update is null");
            throw new IllegalArgumentException("Update is null");
        }
        if (update.hasMessage()) {
            var message = update.getMessage();
            if (message.hasText()) {
                return processTextMessage(update);
            } else {
                log.error("Received empty message " + update);
                return messageUtils.generateSendMessageWithText(update,
                        "Your message is empty");
            }
        } else {
            log.error("Received unsupported message type " + update);
            return messageUtils.generateSendMessageWithText(update,
                    "Unsupported message type");
        }
    }

    private SendMessage processTextMessage(Update update) {
        var text = updateHandler.processText(botUserService.findOrSaveAppUser(update),
                update.getMessage().getText());
        return messageUtils.generateSendMessageWithText(update, text);
    }
}
