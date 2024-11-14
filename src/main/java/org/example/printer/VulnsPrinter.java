package org.example.printer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class VulnsPrinter {

    private static final Logger logger = LogManager.getLogger(VulnsPrinter.class);

    private static final String vulnsOutputPath = "output/vulns.xlsx";


    public static void generateExcel(String response) {
        Workbook workbook = null;
        Sheet sheet;
        try {
            File file = new File(vulnsOutputPath);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    workbook = new XSSFWorkbook(fis);
                    sheet = workbook.getSheetAt(0);
                }
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Vulnerability Data");

                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Entry Method");
                headerRow.createCell(1).setCellValue("Has Vulnerability");
                headerRow.createCell(2).setCellValue("Vulnerability Type");
                headerRow.createCell(3).setCellValue("Trigger Points");
                headerRow.createCell(4).setCellValue("Confidence Score");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);

            int lastRowNum = sheet.getLastRowNum();
            Row dataRow = sheet.createRow(lastRowNum + 1);
            dataRow.createCell(0).setCellValue(rootNode.get("entryMethod").asText());
            dataRow.createCell(1).setCellValue(rootNode.get("hasVul").asBoolean());
            dataRow.createCell(2).setCellValue(rootNode.get("vulType").asText());

            JsonNode triggerPointsNode = rootNode.get("triggerPoints");
            StringBuilder triggerPoints = new StringBuilder();
            Iterator<JsonNode> elements = triggerPointsNode.elements();
            while (elements.hasNext()) {
                triggerPoints.append(elements.next().asText()).append("\n");
            }
            dataRow.createCell(3).setCellValue(triggerPoints.toString());
            dataRow.createCell(4).setCellValue(rootNode.get("confidenceScore").asInt());

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(vulnsOutputPath)) {
                workbook.write(fileOut);
            }

        } catch (IOException e) {
            logger.error("Error processing Excel file: {}", e.getMessage());
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    logger.error("Error closing workbook: {}", e.getMessage());
                }
            }
        }
    }
}