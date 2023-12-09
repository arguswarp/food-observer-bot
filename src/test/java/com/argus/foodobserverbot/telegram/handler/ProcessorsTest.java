package com.argus.foodobserverbot.telegram.handler;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.repository.BotUserRepository;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.repository.FoodRecordRepository;
import com.argus.foodobserverbot.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Optional;

import static com.argus.foodobserverbot.entity.enums.UserState.BASIC_STATE;
import static com.argus.foodobserverbot.entity.enums.UserState.INPUT_FOOD;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ProcessorsTest {
    @Mock
    private BotUserRepository botUserRepository;
    @Mock
    private DayRepository dayRepository;
    @Mock
    private FoodRecordRepository foodRecordRepository;
    @Mock
    private MenuService menuService;

    @InjectMocks
    private CommandProcessor commandProcessor;

    @InjectMocks
    private UserStateProcessor userStateProcessor;

    @Test
    void WhenCancelTextMessage_ReturnCancelMessage() {
        var botUser = BotUser.builder()
                .id(1L)
                .name("Porfyriy")
                .userState(BASIC_STATE)
                .telegramId(36L)
                .build();

        var message = new Message();
        message.setText(CANCEL.getCommand());
        message.setChat(new Chat(1234L, ""));

        Mockito.when(botUserRepository.save(Mockito.any(BotUser.class))).thenReturn(botUser);

        SendMessage sendMessage = (SendMessage) commandProcessor.process(message, botUser);
        assertEquals("1234", sendMessage.getChatId());
        assertEquals("Command canceled", sendMessage.getText());
    }

    @Test
    void WhenDayTextMessage_ReturnStartedDayMessage() {
        var botUser = BotUser.builder()
                .id(1L)
                .name("Porfyriy")
                .userState(BASIC_STATE)
                .telegramId(36L)
                .build();

        var message = new Message();
        message.setText(DAY.getCommand());
        message.setChat(new Chat(1234L, ""));

        Mockito.when(dayRepository.existsDayByDateIs(Mockito.any())).thenReturn(false);
        Mockito.when(dayRepository.save(Mockito.any(Day.class))).thenReturn(Day.builder().build());

        SendMessage sendMessage = (SendMessage) commandProcessor.process(message, botUser);
        assertEquals("1234", sendMessage.getChatId());
        assertEquals("You have started this day's record",
                sendMessage.getText());
    }

    @Test
    void WhenFoodTextMessage_ChangeUserState() {
        var botUser = BotUser.builder()
                .id(1L)
                .name("Porfyriy")
                .userState(BASIC_STATE)
                .telegramId(36L)
                .build();

        var message = new Message();
        message.setText(FOOD_RECORD.getCommand());
        message.setChat(new Chat(1234L, ""));

        Mockito.when(botUserRepository.save(Mockito.any(BotUser.class))).thenReturn(botUser);
        Mockito.when(foodRecordRepository.save(Mockito.any(FoodRecord.class))).thenReturn(new FoodRecord());
        Mockito.when(dayRepository.existsDayByDateIs(Mockito.any())).thenReturn(true);
        Mockito.when(dayRepository.findByDate(Mockito.any())).thenReturn(Optional.of(new Day()));
        Mockito.when(menuService.createOneRowReplyKeyboard(Mockito.any(), Mockito.any()))
                .thenReturn(ReplyKeyboardMarkup.builder().build());

        SendMessage sendMessage = (SendMessage) commandProcessor.process(message, botUser);

        assertEquals("1234", sendMessage.getChatId());
        assertEquals("Enter food", sendMessage.getText());
        assertEquals(INPUT_FOOD, botUser.getUserState());

        message.setText("mock food");

        sendMessage = userStateProcessor.process(message, botUser);

        assertEquals("1234", sendMessage.getChatId());
        assertEquals("You added food record", sendMessage.getText());
        assertEquals(BASIC_STATE, botUser.getUserState());
    }

}