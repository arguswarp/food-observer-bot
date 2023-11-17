package com.argus.foodobserverbot;

import com.argus.foodobserverbot.controller.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class FoodObserverBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodObserverBotApplication.class, args);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot bot) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            return botsApi;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }
}
