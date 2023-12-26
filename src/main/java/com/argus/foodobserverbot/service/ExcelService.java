package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.repository.DayRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.*;
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
    public File createFileAllRecords(String path, BotUser botUser) {
        try {
            Path filePath = preparePath(path, botUser.getName());
            if (!filePath.toFile().exists()) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            }
            log.info("User " + botUser.getName() + " created excel file: " + filePath.toAbsolutePath());
            return createFile(filePath, dayRepository.findAllByOrderByDateDesc());
        } catch (IOException e) {
            log.error("Excel all data file error: " + e);
        }
        //TODO: find better solution
        return null;
    }

    @Transactional
    public File createFileUserRecords(String path, BotUser botUser) {
        try {
            Path filePath = preparePath(path, botUser.getName());
            if (!filePath.toFile().exists()) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            }
            log.info("User " + botUser.getName() + " created excel file: " + filePath.toAbsolutePath());
            return createFile(filePath, dayRepository.findByCreatorOrderByDateDesc(botUser));
        } catch (IOException e) {
            log.error("Excel user data file error: " + e);
        }
        //TODO: find better solution
        return null;
    }

    private Path preparePath(String path, String username) throws IOException {
        return Path.of(path, File.separator
                + username + File.separator
                + ALL_DATA_NAME);
    }

    private File createFile(Path path, List<Day> dayList) {
        try (Workbook workbook = new XSSFWorkbook(XSSFWorkbookType.XLSX);
             OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(path))) {

            Sheet sheet = workbook.createSheet("All data");

            sheet.setColumnWidth(0, 25 * 256);
            for (int i = 1; i < 4; i++) {
                sheet.setColumnWidth(i, 15 * 256);
            }

            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);

            int shift = 0;

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Date");
            header.createCell(1).setCellValue("Bloody rating");
            header.createCell(2).setCellValue("Booty pimples");
            header.createCell(3).setCellValue("Face pimples");

            shift++;

            for (Day day : dayList) {
                Row dayRow = sheet.createRow(dayList.indexOf(day) + shift);
                Cell cell = dayRow.createCell(0);
                cell.setCellValue(day.getDate().format(DateTimeFormatter.ISO_DATE));
                dayRow.createCell(1).setCellValue(day.getBloodyRating());
                dayRow.createCell(2).setCellValue(day.getPimpleBootyRating());
                dayRow.createCell(3).setCellValue(day.getPimpleFaceRating());

                List<FoodRecord> foodRecords = day.getFoodRecords();

                if (foodRecords != null) {
                    for (FoodRecord foodRecord : foodRecords) {
                        Row foodRow = sheet.createRow(1 + foodRecords.indexOf(foodRecord) + dayList.indexOf(day) + shift);
                        foodRow.setRowStyle(style);
                        Cell foodCell = foodRow.createCell(0);
                        foodCell.setCellStyle(style);
                        foodCell.setCellValue(foodRecord.getFood());
                    }
                    shift += foodRecords.size();
                }
            }
            workbook.write(outputStream);
            return path.toFile();
        } catch (IOException e) {
            log.error("Excel workbook write error: " + e);
        }
        //TODO: find better solution
        return null;
    }
}