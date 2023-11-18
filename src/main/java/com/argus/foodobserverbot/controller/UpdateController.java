package com.argus.foodobserverbot.controller;


import com.argus.foodobserverbot.service.MainService;
import com.argus.foodobserverbot.utils.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
@Log4j2
public class UpdateController {
    private
    TelegramBot telegramBot;
    private final MessageUtils messageUtils;

    private final MainService mainService;

    public UpdateController(MessageUtils messageUtils, MainService mainService) {
        this.messageUtils = messageUtils;
        this.mainService = mainService;
    }

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Receive update is null");
            return;
        }
        if (update.hasMessage()) {
            distributeMessageByType(update);
        } else {
            log.error("Received unsupported message type " + update);
        }
    }

    private void distributeMessageByType(Update update) {
        var message = update.getMessage();
        if (message.hasText()) {
            processTextMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setUnsupportedMessageTypeView(Update update) {
        setView(messageUtils.generateSendMessageWithText(update, "Unsupported message type"));
    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }


    private void processTextMessage(Update update) {
        setView(messageUtils.generateSendMessageWithText(update,mainService.processTextMessage(update)));
    }
}
