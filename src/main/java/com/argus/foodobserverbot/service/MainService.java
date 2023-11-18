package com.argus.foodobserverbot.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface MainService {
    String processTextMessage(Update update);
}
