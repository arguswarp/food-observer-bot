package com.argus.foodobserverbot.telegram.handler;

import com.argus.foodobserverbot.entity.BotUser;

public interface UpdateHandler {
    //TODO: change to processUpdate. when change tests
    String processText(BotUser botUser, String text);
}
