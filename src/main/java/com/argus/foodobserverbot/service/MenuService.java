package com.argus.foodobserverbot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

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
}
