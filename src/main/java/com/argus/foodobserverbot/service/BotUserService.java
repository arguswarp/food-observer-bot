package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.entity.enums.UserState;
import com.argus.foodobserverbot.exception.EmptyUpdateException;
import com.argus.foodobserverbot.repository.BotUserRepository;
import com.argus.foodobserverbot.repository.DayRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.argus.foodobserverbot.entity.enums.UserRole.USER;
import static com.argus.foodobserverbot.entity.enums.UserState.BASIC_STATE;

@Service
@Log4j2
@Transactional
public class BotUserService {
    private final BotUserRepository botUserRepository;

    private final DayRepository dayRepository;

    public BotUserService(BotUserRepository botUserRepository, DayRepository dayRepository) {
        this.botUserRepository = botUserRepository;
        this.dayRepository = dayRepository;
    }

    public BotUser findOrSaveAppUser(Update update) {
        Optional<User> telegramUser = Optional.empty();
        if (update.hasMessage()) {
            telegramUser = Optional.ofNullable(update.getMessage().getFrom());
        } else if (update.hasCallbackQuery()) {
            telegramUser = Optional.ofNullable(update.getCallbackQuery().getFrom());
        }
        var user = telegramUser.orElseThrow(() -> new EmptyUpdateException("Update is empty"));
        var telegramId = user.getId();
        var name = user.getUserName();
        BotUser persistentBotUser = botUserRepository.findBotUserByTelegramId(telegramId);
        if (persistentBotUser == null) {
            BotUser transientAppUser = BotUser.builder()
                    .telegramId(telegramId)
                    .name(name)
                    .userRole(USER)
                    .userState(BASIC_STATE)
                    .todayMode(true)
                    .build();
            log.info("User is created: {} with id {}", transientAppUser.getName(), transientAppUser.getId());
            return botUserRepository.save(transientAppUser);
        }
        return persistentBotUser;
    }

    public BotUser changeState(BotUser botUser, UserState state) {
        botUser.setUserState(state);
        return botUserRepository.save(botUser);
    }

    public BotUser changeTodayMode(BotUser botUser, boolean mode) {
        botUser.setTodayMode(mode);
        return botUserRepository.save(botUser);
    }
    @Transactional(readOnly = true)
    public List<FoodRecord> getTodayFoodRecords(BotUser botUser) {
        return dayRepository.findByDateAndCreator(LocalDate.now(), botUser)
                .map(Day::getFoodRecords)
                .orElse(List.of());
    }

    public String getTodayNotes(BotUser botUser) {
        return dayRepository.findByDateAndCreator(LocalDate.now(), botUser)
                .map(Day::getNotes)
                .orElse("");
    }

    @Transactional(readOnly = true)
    public LocalDate selectDate(BotUser botUser) {
        return botUser.getTodayMode() ? LocalDate.now() : LocalDate.now().minusDays(1);
    }
}
