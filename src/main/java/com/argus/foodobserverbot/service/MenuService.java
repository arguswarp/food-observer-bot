package com.argus.foodobserverbot.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
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
