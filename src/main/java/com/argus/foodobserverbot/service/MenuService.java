package com.argus.foodobserverbot.service;

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

    public ReplyKeyboard createOneRowReplyKeyboard(List<String> buttonsNames, List<String> callbackDatas) {
        if (!(buttonsNames.size() == callbackDatas.size())) {
            throw new IllegalArgumentException("Button names and callback datas must match");
        }
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (int i = 0; i < buttonsNames.size(); i++) {
            buttons.add(InlineKeyboardButton.builder()
                    .text(buttonsNames.get(i))
                    .callbackData(callbackDatas.get(i))
                    .build());
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(buttons))
                .build();
    }

    public ReplyKeyboard createTwoRowReplyKeyboard(List<String> buttonsRowOne, List<String> callbackDataRowOne,
                                                   List<String> buttonsRowTwo, List<String> callbackDataRowTwo) {
        if (!(buttonsRowOne.size() == callbackDataRowOne.size()) ||
                !(buttonsRowTwo.size() == callbackDataRowTwo.size())) {
            throw new IllegalArgumentException("Button names and callback datas must match");
        }
        List<InlineKeyboardButton> rowOne = new ArrayList<>();
        for (int i = 0; i < buttonsRowOne.size(); i++) {
            rowOne.add(InlineKeyboardButton.builder()
                    .text(buttonsRowOne.get(i))
                    .callbackData(callbackDataRowOne.get(i))
                    .build());
        }
        List<InlineKeyboardButton> rowTwo = new ArrayList<>();
        for (int i = 0; i < buttonsRowTwo.size(); i++) {
            rowTwo.add(InlineKeyboardButton.builder()
                    .text(buttonsRowTwo.get(i))
                    .callbackData(callbackDataRowTwo.get(i))
                    .build());
        }
        return InlineKeyboardMarkup.builder()
                .keyboardRow(rowOne)
                .keyboardRow(rowTwo)
                .build();
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
