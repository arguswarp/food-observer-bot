package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.repository.DayRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Log4j2
public class ExcelService {
    private final DayRepository dayRepository;
    private final String ALL_DATA_NAME = "all_data.xlsx";
    public ExcelService(DayRepository dayRepository) {
        this.dayRepository = dayRepository;
    }

    @Transactional
    public File createExcelFileAllData(String path, BotUser botUser) {
        try {
            Path filePath = Path.of(path, File.separator
                    + botUser.getName() + File.separator
                    + ALL_DATA_NAME);
            if (!filePath.toFile().exists()) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            }
            try (Workbook workbook = new XSSFWorkbook(XSSFWorkbookType.XLSX); OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(filePath))) {
                Sheet sheet = workbook.createSheet("All data");
                List<Day> dayList = dayRepository.findAllByOrderByDateDesc();
                int shift = 0;

                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Date");
                header.createCell(1).setCellValue("Bloody rating");
                header.createCell(2).setCellValue("Booty pimples");
                header.createCell(3).setCellValue("Face pimples");

                shift++;

                for (Day day : dayList) {
                    Row dayRow = sheet.createRow(dayList.indexOf(day) + shift);

                    dayRow.createCell(0).setCellValue(day.getDate().format(DateTimeFormatter.ISO_DATE));
                    dayRow.createCell(1).setCellValue(day.getBloodyRating());
                    dayRow.createCell(2).setCellValue(day.getPimpleBootyRating());
                    dayRow.createCell(3).setCellValue(day.getPimpleFaceRating());

                    List<FoodRecord> foodRecords = day.getFoodRecords();

                    if (foodRecords != null) {
                        for (FoodRecord foodRecord : foodRecords) {
                            Row foodRow = sheet.createRow(1 + foodRecords.indexOf(foodRecord) + dayList.indexOf(day) + shift);
                            foodRow.createCell(0).setCellValue(foodRecord.getFood());
                        }
                        shift += foodRecords.size();
                    }
                }
                workbook.write(outputStream);
                return filePath.toFile();
            } catch (IOException e) {
                log.error("Excel workbook write error: " + e);
            }
        } catch (IOException e) {
            log.error("Excel all data file error: " + e);
        }
        //TODO: mb change to something better
        return null;
    }
}
