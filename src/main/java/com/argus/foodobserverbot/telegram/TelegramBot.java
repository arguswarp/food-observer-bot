package com.argus.foodobserverbot.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
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

@RequiredArgsConstructor
@Component
@Log4j2
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String token;

    @Value("${bot.name}")
    private String name;

    private final ApplicationEventPublisher publisher;

    private boolean prevSendMessageHasCallbackQuery;

    @Override
    public void onUpdateReceived(Update update) {

        publisher.publishEvent(new UpdateEvent(update, UpdateEvent.Type.RECEIVED));

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
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @EventListener(value = UpdateEvent.class, condition = "#event.type.name() == 'PROCESSED'")
    private void consume(UpdateEvent event) {
        var update = (PartialBotApiMethod<?>) event.getSource();
        sendAnswerMessage(update);
    }

    private void sendAnswerMessage(PartialBotApiMethod<?> message) {
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

    private void executeBotApiMethod(Supplier<BotApiMethod<?>> handler) {
        try {
            execute(handler.get());
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }
}
