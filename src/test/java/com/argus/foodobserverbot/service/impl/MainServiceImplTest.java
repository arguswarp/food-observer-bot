package com.argus.foodobserverbot.service.impl;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.repository.BotUserRepository;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.repository.FoodRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.argus.foodobserverbot.entity.enums.UserState.BASIC_STATE;
import static com.argus.foodobserverbot.entity.enums.UserState.WAIT_FOR_INPUT_FOOD;
import static com.argus.foodobserverbot.service.enums.ServiceCommands.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MainServiceImplTest {
    @Mock
    private BotUserRepository botUserRepository;
    @Mock
    private DayRepository dayRepository;
    @Mock
    private FoodRecordRepository foodRecordRepository;
    @InjectMocks
    private MainServiceImpl mainService;

    @Test
    void WhenCancelTextMessage_ReturnCancelMessage() {
        var botUser = BotUser.builder()
                .id(1L)
                .name("Porfyriy")
                .userState(BASIC_STATE)
                .telegramId(36L)
                .build();
        String textToProcess = CANCEL.getCommand();
        Mockito.when(botUserRepository.save(Mockito.any(BotUser.class))).thenReturn(botUser);
        assertEquals("Command canceled!", mainService.processText(botUser, textToProcess));
    }

    @Test
    void WhenDayTextMessage_ReturnStartedDayMessage() {
        var botUser = BotUser.builder()
                .id(1L)
                .name("Porfyriy")
                .userState(BASIC_STATE)
                .telegramId(36L)
                .build();
        String textToProcess = DAY.getCommand();
        Mockito.when(dayRepository.existsDayByDateIs(Mockito.any())).thenReturn(false);
        Mockito.when(dayRepository.save(Mockito.any(Day.class))).thenReturn(Day.builder().build());
        assertEquals("You have started this day's record", mainService.processText(botUser, textToProcess));
    }

    @Test
    void WhenFoodTextMessage_ChangeUserState() {
        var botUser = BotUser.builder()
                .id(1L)
                .name("Porfyriy")
                .userState(BASIC_STATE)
                .telegramId(36L)
                .build();
        String textToProcess = FOOD_RECORD.getCommand();

        Mockito.when(botUserRepository.save(Mockito.any(BotUser.class))).thenReturn(botUser);
        Mockito.when(foodRecordRepository.save(Mockito.any(FoodRecord.class))).thenReturn(new FoodRecord());
        Mockito.when(dayRepository.existsDayByDateIs(Mockito.any())).thenReturn(true);
        Mockito.when(dayRepository.findByDate(Mockito.any())).thenReturn(Optional.of(new Day()));

        assertEquals("Enter food", mainService.processText(botUser, textToProcess));
        assertEquals(WAIT_FOR_INPUT_FOOD, botUser.getUserState());

        textToProcess = "mock food";
        assertEquals("You added food record", mainService.processText(botUser, textToProcess));
        assertEquals(BASIC_STATE, botUser.getUserState());
    }

}