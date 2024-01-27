package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.telegram.enums.ServiceCommands;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.*;

@Service
public class MenuService {

    public ReplyKeyboard createOneRowReplyKeyboard(ServiceCommands... commands) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(addRow(List.of(commands)))
                .build();
    }

    public ReplyKeyboard createTwoRowReplyKeyboard(List<ServiceCommands> commandsRowOne, List<ServiceCommands> commandsRowTwo) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(addRow(commandsRowOne))
                .keyboardRow(addRow(commandsRowTwo))
                .build();
    }

    private List<InlineKeyboardButton> addRow(List<ServiceCommands> commands) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        commands.forEach(command -> row.add(InlineKeyboardButton.builder()
                .text(command.getButtonName())
                .callbackData(command.getCommand()
                )
                .build()));
        return row;
    }

    public ReplyKeyboard createMainMenu() {
        return ReplyKeyboardMarkup.builder()
                .keyboardRow(new KeyboardRow(List.of(
                        KeyboardButton.builder()
                                .text(FOOD_RECORD.getButtonName())
                                .build(),
                        KeyboardButton.builder()
                                .text(IS_BLOOD.getButtonName())
                                .build(),
                        KeyboardButton.builder()
                                .text(IS_PIMPLE.getButtonName())
                                .build(),
                        KeyboardButton.builder()
                                .text(MODE.getButtonName())
                                .build()
                )))
                .resizeKeyboard(true)
                .build();
    }

    public boolean validateMainMenuReply(String text) {
        return List.of(FOOD_RECORD.getButtonName(),
                IS_BLOOD.getButtonName(),
                IS_PIMPLE.getButtonName(),
                MODE.getButtonName()).contains(text);
    }

    public String toServiceCommand(String text) {
        return "/" + text.toLowerCase();
    }
}
