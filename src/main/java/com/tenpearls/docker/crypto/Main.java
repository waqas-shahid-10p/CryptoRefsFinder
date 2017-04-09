package com.tenpearls.docker.crypto;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    private final static Pattern CRYPTO_IMPORT = Pattern.compile("\"crypto/([A-za-z0-9]+)\"");
    private final static String USAGE_PATTERN = "(%s)\\.([A-Za-z0-9_])+";

    private static class Options {
        private final String parentPath;
        private boolean recursive = true;

        private Options() {
            if (System.getProperty("path") == null) {
                this.parentPath = System.getProperty("user.dir");
            } else {
                this.parentPath = System.getProperty("path");
            }
            System.out.println("Path to search: " + this.parentPath);
        }
    }


    private static class FileMatches {
        private final String filePath;
        private final Set<String> algorithms = new HashSet<>();
        private final List<String> matches = new ArrayList<>();

        public FileMatches(final String filePath) {
            this.filePath = filePath;
        }
    }

    public static void main(final String[] args) throws IOException {
        final Options options = new Options();
        final Collection<File> files = FileUtils.listFiles(new File(options.parentPath), new String[]{"go"}, true);
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Results");
        final Font font = workbook.createFont();
        font.setBold(true);
        final CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0, CellType.STRING);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("File Name");
        cell = row.createCell(1, CellType.STRING);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("File Path");
        cell = row.createCell(2, CellType.STRING);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Algorithm");
        cell = row.createCell(3, CellType.STRING);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Instance");
        cell = row.createCell(4, CellType.STRING);
        cell.setCellStyle(cellStyle);
        cell.setCellValue("Line number");

        int rowNum = 1;
        for (final File file : files) {
            final FileMatches fileMatches = new FileMatches(file.getPath());
            try {
                final LineIterator lineIterator = FileUtils.lineIterator(file);
                Pattern pattern = null;
                int lineNum = 1;
                boolean firstMatch = false;
                while (lineIterator.hasNext()) {
                    final String line = lineIterator.nextLine();
                    Matcher matcher = CRYPTO_IMPORT.matcher(line);
                    if (matcher.find()) {
                        fileMatches.algorithms.add(matcher.group(1));
                        pattern = Pattern.compile(String.format(USAGE_PATTERN, String.join("|", fileMatches.algorithms)));
                        if (!firstMatch) {
                            row = sheet.createRow(rowNum++);
                            firstMatch = true;
                        }
                    } else if (pattern != null) {
                        matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            fileMatches.matches.add(matcher.group(0));
                            cell = row.createCell(0, CellType.STRING);
                            cell.setCellValue(file.getName());
                            cell.getCellStyle().setWrapText(true);
                            cell = row.createCell(1, CellType.STRING);
                            cell.setCellValue(file.getPath());
                            cell.getCellStyle().setWrapText(true);
                            cell = row.createCell(2, CellType.STRING);
                            cell.setCellValue(matcher.group(1));
                            cell.getCellStyle().setWrapText(true);
                            cell = row.createCell(3, CellType.STRING);
                            cell.setCellValue(matcher.group(0));
                            cell.getCellStyle().setWrapText(true);
                            cell = row.createCell(4, CellType.STRING);
                            cell.setCellValue(file.getName() + ":" + lineNum);
                            cell.getCellStyle().setWrapText(true);
                        }
                    }
                    lineNum++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            sheet.autoSizeColumn(i);
        }
        FileOutputStream sheetFile = new FileOutputStream(new File("References.xslx"));
        workbook.write(sheetFile);
        workbook.close();
        System.out.println("References.xslx generated successfully");
    }
}