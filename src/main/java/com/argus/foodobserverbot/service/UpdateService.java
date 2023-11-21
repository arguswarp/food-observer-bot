package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.repository.BotUserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.argus.foodobserverbot.entity.enums.UserState.BASIC_STATE;

@Service
@Log4j2
public class UpdateService {
    private final BotUserRepository botUserRepository;

    public UpdateService(BotUserRepository botUserRepository) {
        this.botUserRepository = botUserRepository;
    }

    public BotUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        BotUser persistentBotUser = botUserRepository.findBotUserByTelegramId(telegramUser.getId());
        if (persistentBotUser == null) {
            BotUser transientAppUser = BotUser.builder()
                    .telegramId(telegramUser.getId())
                    .name(telegramUser.getUserName())
                    .userState(BASIC_STATE)
                    .build();
            return botUserRepository.save(transientAppUser);
        }
        return persistentBotUser;
    }
}
