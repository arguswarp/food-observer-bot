package com.argus.foodobserverbot.telegram.handler;

import com.argus.foodobserverbot.entity.BotUser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
@Component
public interface UpdateHandler<T> {
    PartialBotApiMethod<?> handleUpdate(T t, BotUser botUser);
}
