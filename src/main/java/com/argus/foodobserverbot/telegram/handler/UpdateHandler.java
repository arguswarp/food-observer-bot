package com.argus.foodobserverbot.telegram.handler;

import com.argus.foodobserverbot.entity.BotUser;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

public interface UpdateHandler<T> {
    PartialBotApiMethod<?> handleUpdate(T t, BotUser botUser);
}
