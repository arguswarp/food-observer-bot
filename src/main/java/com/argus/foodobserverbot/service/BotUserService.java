package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.exception.EmptyUpdateException;
import com.argus.foodobserverbot.repository.BotUserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static com.argus.foodobserverbot.entity.enums.UserState.BASIC_STATE;

@Service
@Log4j2
public class BotUserService {
    private final BotUserRepository botUserRepository;

    public BotUserService(BotUserRepository botUserRepository) {
        this.botUserRepository = botUserRepository;
    }

    public BotUser findOrSaveAppUser(Update update) {
        Optional<User> telegramUser = Optional.empty();
        if (update.hasMessage()) {
            telegramUser = Optional.ofNullable(update.getMessage().getFrom());
        } else if (update.hasCallbackQuery()) {
            telegramUser = Optional.ofNullable(update.getCallbackQuery().getMessage().getFrom());
        }
        var user = telegramUser.orElseThrow(() -> new EmptyUpdateException("Update is empty"));
        var telegramId = user.getId();
        var name = user.getUserName();
        BotUser persistentBotUser = botUserRepository.findBotUserByTelegramId(telegramId);
        if (persistentBotUser == null) {
            BotUser transientAppUser = BotUser.builder()
                    .telegramId(telegramId)
                    .name(name)
                    .userState(BASIC_STATE)
                    .build();
            return botUserRepository.save(transientAppUser);
        }
        return persistentBotUser;
    }
}
