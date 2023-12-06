package com.argus.foodobserverbot.telegram;

import com.argus.foodobserverbot.controller.UpdateController;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Log4j2
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${bot.token}")
    private String token;
    @Value("${bot.name}")
    private String name;
    private final UpdateController updateController;

    public TelegramBot(UpdateController updateController) {
        this.updateController = updateController;
    }

    @Override
    public void onUpdateReceived(Update update) {
        sendAnswerMessage(updateController.processUpdate(update));
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    public void sendAnswerMessage(PartialBotApiMethod<?> message) {

        if (message != null) {
            try {
                if (message instanceof SendMessage) {
                    execute((SendMessage)message);
                } else if (message instanceof SendDocument) {
                    execute((SendDocument) message);
                }
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }
}
