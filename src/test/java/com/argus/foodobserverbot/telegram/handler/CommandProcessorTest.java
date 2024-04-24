package com.argus.foodobserverbot.telegram.handler;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.service.BotUserService;
import com.argus.foodobserverbot.service.DayService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;

import static com.argus.foodobserverbot.entity.enums.UserState.BASIC_STATE;
import static com.argus.foodobserverbot.entity.enums.UserState.INPUT_FOOD;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.CANCEL;
import static com.argus.foodobserverbot.telegram.enums.ServiceCommands.FOOD_RECORD;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CommandProcessorTest {

    @Mock
    private BotUserService botUserService;

    @Mock
    private DayService dayService;

    @InjectMocks
    private CommandProcessor commandProcessor;

    private final Long CHAT_ID = 1234L;
    private final Long ID = 36L;
    private final Long TG_ID = 9L;

    private final String NAME = "Porfiry";

    @Test
    void WhenCancelTextMessage_ReturnMessage() {
        var botUser = BotUser.builder()
                .id(ID)
                .name(NAME)
                .userState(BASIC_STATE)
                .telegramId(TG_ID)
                .build();

        Mockito.when(botUserService.changeState(Mockito.any(BotUser.class), Mockito.any())).thenReturn(botUser);

        SendMessage sendMessage = (SendMessage) commandProcessor.process(botUser, CHAT_ID, CANCEL.getCommand());

        assertEquals(CHAT_ID.toString(), sendMessage.getChatId());
        assertEquals("Command canceled", sendMessage.getText());
    }

    @Test
    void WhenFoodTextMessage_ReturnMessage() {
        var botUser = BotUser.builder()
                .id(ID)
                .name(NAME)
                .userState(INPUT_FOOD)
                .todayMode(true)
                .telegramId(TG_ID)
                .build();

        var day = Day.builder()
                .date(LocalDate.now())
                .build();

        Mockito.when(dayService.findOrSaveDay(Mockito.any(BotUser.class),
                Mockito.any())).thenReturn(day);
        Mockito.when(botUserService.changeState(Mockito.any(BotUser.class), Mockito.any()))
                .thenReturn(botUser);

        SendMessage sendMessage = (SendMessage) commandProcessor.process(botUser, CHAT_ID, FOOD_RECORD.getCommand());

        assertEquals(CHAT_ID.toString(), sendMessage.getChatId());
        assertEquals("Enter food for today", sendMessage.getText());
    }
}