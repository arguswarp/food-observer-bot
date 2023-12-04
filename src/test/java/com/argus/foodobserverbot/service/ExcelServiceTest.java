package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.repository.DayRepository;
import com.argus.foodobserverbot.repository.FoodRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ExcelServiceTest {
    @Mock
    private FoodRecordRepository foodRecordRepository;

    @Mock
    private DayRepository dayRepository;

    @InjectMocks
    private ExcelService excelService;

    private final String FILE_PATH = "./excel-test/test_table.xlsx";

    @Test
    void WhenCreateExcelFileAllData_FileIsCreated() {

        try {
            Files.deleteIfExists(Path.of(FILE_PATH));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<FoodRecord> foodRecords1 = List.of(
                FoodRecord.builder()
                        .food("pizza")
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

        List <Day> dayRecords = List.of(
                Day.builder()
                        .date(LocalDate.now())
                        .bloodyRating(5)
                        .pimpleBootyRating(6)
                        .pimpleFaceRating(3)
                        .foodRecords(foodRecords1)
                        .build(),
                Day.builder()
                        .date(LocalDate.of(2023,12,1))
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

        Mockito.when(dayRepository.findAll()).thenReturn(dayRecords);

        excelService.createExcelFileAllData(FILE_PATH);
    }
}
