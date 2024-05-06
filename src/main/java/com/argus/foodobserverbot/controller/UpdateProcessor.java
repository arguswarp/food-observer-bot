package com.argus.foodobserverbot.controller;

import com.argus.foodobserverbot.exception.EmptyUpdateException;
import com.argus.foodobserverbot.service.BotUserService;
import com.argus.foodobserverbot.telegram.UpdateEvent;
import com.argus.foodobserverbot.telegram.handler.UpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Component
@Log4j2
public class UpdateProcessor {

    private final UpdateHandler<CallbackQuery> callbackQueryHandler;

    private final UpdateHandler<Message> messageHandler;

    private final BotUserService botUserService;

    private final ApplicationEventPublisher publisher;

    @EventListener(value = UpdateEvent.class, condition = "#event.type.name() == 'RECEIVED'")
    public void produce(UpdateEvent event) {
        var update = (Update) event.getSource();
        if (update == null) {
            log.error("Received update is null");
            throw new EmptyUpdateException("Update is null");
        }
        var botUser = botUserService.findOrSaveAppUser(update);
        PartialBotApiMethod<?> processedUpdate;
        if (update.hasCallbackQuery()) {
            processedUpdate = callbackQueryHandler.handleUpdate(update.getCallbackQuery(), botUser);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            processedUpdate = messageHandler.handleUpdate(update.getMessage(), botUser);
        } else {
            log.error("Received unsupported message type {}", update);
            processedUpdate = SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Unsupported message type")
                    .build();
        }
        publisher.publishEvent(new UpdateEvent(processedUpdate, UpdateEvent.Type.PROCESSED));
    }
}
