package com.argus.foodobserverbot.telegram;

import com.argus.foodobserverbot.controller.UpdateController;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Supplier;

@Component
@Log4j2
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String token;

    @Value("${bot.name}")
    private String name;

    private final UpdateController updateController;

    private boolean prevSendMessageHasCallbackQuery;

    public TelegramBot(UpdateController updateController) {
        this.updateController = updateController;
    }

    @Override
    public void onUpdateReceived(Update update) {
        //delete messages with inline keyboard after key pressed
        if (update.hasCallbackQuery()) {
            executeBotApiMethod(() -> DeleteMessage.builder()
                    .chatId(update.getCallbackQuery().getMessage().getChatId())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
        }
        //remove keyboard from message after input
        if (update.hasMessage() && prevSendMessageHasCallbackQuery) {
            executeBotApiMethod(() -> EditMessageReplyMarkup.builder()
                    .replyMarkup(InlineKeyboardMarkup.builder()
                            .clearKeyboard()
                            .build())
                    .chatId(update.getMessage().getChatId())
                    .messageId(update.getMessage().getMessageId() - 1)
                    .build());
        }

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
                if (message instanceof SendMessage sendMessage) {
                    execute(sendMessage);
                    prevSendMessageHasCallbackQuery = sendMessage.getReplyMarkup() != null;
                } else if (message instanceof SendDocument sendDocument) {
                    execute(sendDocument);
                    prevSendMessageHasCallbackQuery = sendDocument.getReplyMarkup() != null;
                }
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }

    public void executeBotApiMethod(Supplier<BotApiMethod<?>> handler) {
        try {
            execute(handler.get());
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }
}
