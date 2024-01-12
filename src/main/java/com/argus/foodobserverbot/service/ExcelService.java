package com.argus.foodobserverbot.service;

import com.argus.foodobserverbot.entity.BotUser;
import com.argus.foodobserverbot.entity.Day;
import com.argus.foodobserverbot.entity.FoodRecord;
import com.argus.foodobserverbot.repository.DayRepository;
import lombok.NonNull;
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
    private final String EXTENSION = ".xlsx";

    public ExcelService(DayRepository dayRepository) {
        this.dayRepository = dayRepository;
    }
    @Transactional
    public File createExcelAllRecords(String path, BotUser botUser) {
        try {
            Path filePath = preparePath(path, botUser.getName(), "all_data");
            createFileWithDirectory(filePath);
            log.info("User " + botUser.getName() + " created excel file: " + filePath.toAbsolutePath());
            return generateExcel(filePath, dayRepository.findAllByOrderByDateDesc());
        } catch (IOException e) {
            log.error("Excel all data file error: " + e);
        }
        //TODO: find better solution
        return null;
    }
    @Transactional
    public File createExcelUserRecords(String path, BotUser botUser) {
        try {
            String name = botUser.getName();
            Path filePath = preparePath(path, name, name + "_data");
            createFileWithDirectory(filePath);
            log.info("User " + botUser.getName() + " created excel file: " + filePath.toAbsolutePath());
            var days = dayRepository.findByCreatorOrderByDateDesc(botUser);
            return generateExcel(filePath, days);
        } catch (IOException e) {
            log.error("Excel user data file error: " + e);
        }
        //TODO: find better solution
        return null;
    }

    private Path preparePath(String path, String username, String filename) throws IOException {
        return Path.of(path, File.separator
                + username + File.separator
                + filename + EXTENSION);
    }

    private void createFileWithDirectory(Path path) {
        if (!path.toFile().exists()) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e) {
                log.error("Error while creating file " + e);
            }
        }
    }

    private File generateExcel(Path path, @NonNull List<Day> dayList) {
        try (Workbook workbook = new XSSFWorkbook(XSSFWorkbookType.XLSX);
             OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(path))) {

            Sheet sheet = workbook.createSheet("Data");

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