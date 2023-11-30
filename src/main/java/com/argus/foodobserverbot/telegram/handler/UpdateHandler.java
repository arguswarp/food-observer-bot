package com.argus.foodobserverbot.telegram.handler;

import com.argus.foodobserverbot.entity.BotUser;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler <T> {
    SendMessage handleUpdate(T t, BotUser botUser);
}