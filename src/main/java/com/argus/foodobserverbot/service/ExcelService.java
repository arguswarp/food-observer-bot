package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.repository.DayRepository;
import liquibase.resource.OpenOptions;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookType;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Sort;
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


    public ExcelService(DayRepository dayRepository) {
        this.dayRepository = dayRepository;
    }
    @Transactional
    public File createExcelFileAllData(String path) {
        Path filePath = Path.of(path);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (Workbook workbook = new XSSFWorkbook(XSSFWorkbookType.XLSX);
             OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(Files.createFile(filePath)))) {
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
            return new File(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
