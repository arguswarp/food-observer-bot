package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ExcelServiceTest {

    @InjectMocks
    private ExcelService excelService;

    private final String FILE_PATH = "excel-test";

    @Test
    void WhenCreateExcelFileAllData_FileIsCreated() {
        List<FoodRecord> foodRecords1 = List.of(
                FoodRecord.builder()
                        .food("&ZXuH+GH1F &Vce%O!%0u p%=g#3VEKn Y$wqbgbPka gyazCVQW0e jJtCczc75q 2R@=dA9nmP 0JF5Q@s&6R =mYYzu5SJ!\n xV*&AV*J*x")
                        .build(),
                FoodRecord.builder()
                        .food("burger")
                        .build(),
                FoodRecord.builder()
                        .food("sushi")
                        .build());

        List<FoodRecord> foodRecords2 = List.of(
                FoodRecord.builder()
                        .food("pasta")
                        .build());

        List<Day> dayRecords = List.of(
                Day.builder()
                        .date(LocalDate.now())
                        .bloodyRating(5)
                        .pimpleBootyRating(6)
                        .pimpleFaceRating(3)
                        .foodRecords(foodRecords1)
                        .build(),
                Day.builder()
                        .date(LocalDate.of(2023, 12, 1))
                        .bloodyRating(3)
                        .pimpleBootyRating(3)
                        .pimpleFaceRating(0)
                        .foodRecords(foodRecords2)
                        .build(),
                Day.builder()
                        .date(LocalDate.of(2023, 11, 20))
                        .bloodyRating(1)
                        .pimpleBootyRating(1)
                        .pimpleFaceRating(1)
                        .build()
        );
        BotUser user = BotUser.builder()
                .name("Porfiriy")
                .days(dayRecords)
                .build();
        excelService.createExcelUserRecords(FILE_PATH, user);
    }
}
