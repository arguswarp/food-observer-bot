package com.argus.foodobserverbot.telegram.handler.impl;

import com.argus.foodobserverbot.entity.BotUser;
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

    public MessageHandler(CommandProcessor commandProcessor, UserStateProcessor stateProcessor) {
        this.commandProcessor = commandProcessor;
        this.stateProcessor = stateProcessor;
    }

    @Override
    public PartialBotApiMethod<?> handleUpdate(Message message, BotUser botUser) {
        String text = message.getText();
        if (ServiceCommands.isCommand(text) && (text.equals(CANCEL.getCommand()) || botUser.getUserState().equals(BASIC_STATE))) {
            return commandProcessor.process(message, botUser);
        }
        return stateProcessor.process(message, botUser);
    }
}
