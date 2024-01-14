package com.argus.foodobserverbot.config;

import com.argus.foodobserverbot.telegram.TelegramBot;
import com.argus.foodobserverbot.telegram.enums.ServiceCommands;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Log4j2
public class BotConfig {
    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot bot) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            addMainMenu(bot);
            log.info("Bot successfully initialized");
            return botsApi;
        } catch (TelegramApiException e) {
            log.error("Error while bot initialization " + e.getMessage());
        }
        return null;
    }

    private void addMainMenu(TelegramBot bot){
        List<BotCommand> commandList = new ArrayList<>();
        for (ServiceCommands c : ServiceCommands.values()) {
            commandList.add(new BotCommand(c.getCommand(), c.getDescription()));
        }
        try {
            bot.execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error while initializing main menu " + e.getMessage());
        }
    }
}
