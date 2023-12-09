package com.argus.foodobserverbot.telegram.handler.impl;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.service.MenuService;
import com.argus.foodobserverbot.telegram.enums.ServiceCommands;
import com.argus.foodobserverbot.telegram.handler.CommandProcessor;
import com.argus.foodobserverbot.telegram.handler.UpdateHandler;
import com.argus.foodobserverbot.telegram.handler.UserStateProcessor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.argus.foodobserverbot.entity.enums.UserState.BASIC_STATE;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.CANCEL;

@Component
public class MessageHandler implements UpdateHandler<Message> {
    private final CommandProcessor commandProcessor;
    private final UserStateProcessor stateProcessor;
    private final MenuService menuService;

    public MessageHandler(CommandProcessor commandProcessor, UserStateProcessor stateProcessor, MenuService menuService) {
        this.commandProcessor = commandProcessor;
        this.stateProcessor = stateProcessor;
        this.menuService = menuService;
    }

    @Override
    public PartialBotApiMethod<?> handleUpdate(Message message, BotUser botUser) {
        var text = message.getText();
        if (menuService.validateMainMenuReply(text)) {
            text = menuService.toServiceCommand(text);
        }
        var chatId = message.getChatId();
        if (ServiceCommands.isCommand(text) && (text.equals(CANCEL.getCommand()) || botUser.getUserState().equals(BASIC_STATE))) {
            return commandProcessor.process(botUser, chatId, text);
        }
        return stateProcessor.process(botUser, chatId, text);
    }
}
