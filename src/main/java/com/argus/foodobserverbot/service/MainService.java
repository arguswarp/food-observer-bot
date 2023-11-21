package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.BotUser;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface MainService {
    String processText(BotUser botUser, String text);
}
