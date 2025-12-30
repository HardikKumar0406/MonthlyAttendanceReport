package attendanceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExporter {

    // ===================== MODEL =====================
    public static class AttendanceRecord {
        public String firstName;
        public String lastName;
        public String accessTime;   // API timestamp
        public String checkType;

        public AttendanceRecord(String firstName, String lastName,
                                String accessTime, String checkType) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.accessTime = accessTime;
            this.checkType = checkType;
        }
    }

    // ===================== FORMATTERS =====================
    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter OUTPUT_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ===================== ENV DETECTION =====================
    private static final boolean IS_CLI_RUN =
            "true".equalsIgnoreCase(System.getenv("GITHUB_ACTIONS"));

    // ===================== TIME HANDLER =====================
    private static LocalDateTime resolveTime(String accessTime) {
        LocalDateTime dt = LocalDateTime.parse(accessTime, INPUT_FORMAT);
        return IS_CLI_RUN ? dt.plusMinutes(330) : dt;
    }

    // ===================== EXPORT METHOD =====================
    public static String exportAttendance(List<AttendanceRecord> records, String folderPath) {

        try (Workbook workbook = new XSSFWorkbook()) {

            // ===================== STYLES =====================
            Font greenFont = workbook.createFont();
            greenFont.setBold(true);
            greenFont.setColor(IndexedColors.GREEN.getIndex());

            Font orangeFont = workbook.createFont();
            orangeFont.setBold(true);
            orangeFont.setColor(IndexedColors.ORANGE.getIndex());

            Font redFont = workbook.createFont();
            redFont.setBold(true);
            redFont.setColor(IndexedColors.RED.getIndex());

            CellStyle greenStyle = workbook.createCellStyle();
            greenStyle.setFont(greenFont);

            CellStyle orangeStyle = workbook.createCellStyle();
            orangeStyle.setFont(orangeFont);

            CellStyle redStyle = workbook.createCellStyle();
            redStyle.setFont(redFont);

            // ===================== GROUP BY DATE =====================
            Map<String, List<AttendanceRecord>> groupedByDate = new TreeMap<>();

            for (AttendanceRecord rec : records) {
                LocalDateTime dt = resolveTime(rec.accessTime);
                String dateKey = dt.toLocalDate().format(DATE_FORMAT);
                groupedByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(rec);
            }

            // ===================== GLOBAL LATE COUNTER =====================
            Map<String, Integer> lateCountMap = new TreeMap<>();

            // ===================== CREATE DATE SHEETS =====================
            for (Map.Entry<String, List<AttendanceRecord>> entry : groupedByDate.entrySet()) {

                String date = entry.getKey();
                List<AttendanceRecord> dayRecords = entry.getValue();

                Sheet sheet = workbook.createSheet(date);

                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("First Name");
                header.createCell(1).setCellValue("Last Name");
                header.createCell(2).setCellValue("Access Time (IST)");
                header.createCell(3).setCellValue("Tag");
                header.createCell(4).setCellValue("Attendance Status");

                dayRecords.sort(Comparator.comparing(r -> resolveTime(r.accessTime)));

                Map<String, List<AttendanceRecord>> userMap = new HashMap<>();
                for (AttendanceRecord rec : dayRecords) {
                    String key = rec.firstName.trim() + "_" + rec.lastName.trim();
                    userMap.computeIfAbsent(key, k -> new ArrayList<>()).add(rec);
                }

                userMap.values()
                       .forEach(list ->
                               list.sort(Comparator.comparing(r -> resolveTime(r.accessTime)))
                       );

                Map<String, Integer> userIndex = new HashMap<>();
                int rowIndex = 1;

                for (AttendanceRecord rec : dayRecords) {

                    String key = rec.firstName.trim() + "_" + rec.lastName.trim();
                    int index = userIndex.getOrDefault(key, 0);

                    AttendanceRecord orderedRec = userMap.get(key).get(index);
                    LocalDateTime dt = resolveTime(orderedRec.accessTime);

                    String tag;
                    String status = "";

                    if (index == 0) {
                        tag = "Clock-in";
                        LocalTime time = dt.toLocalTime();

                        if (!time.isAfter(LocalTime.of(10, 0))) {
                            status = "On-time";
                        } else if (!time.isAfter(LocalTime.of(10, 15))) {
                            status = "Buffer Late";
                        } else {
                            status = "Late";
                            lateCountMap.put(key, lateCountMap.getOrDefault(key, 0) + 1);
                        }
                    } else {
                        tag = "Check-Out".equalsIgnoreCase(orderedRec.checkType)
                                ? "Clock-out" : "Break";
                    }

                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(rec.firstName);
                    row.createCell(1).setCellValue(rec.lastName);
                    row.createCell(2).setCellValue(dt.format(OUTPUT_DATETIME));
                    row.createCell(3).setCellValue(tag);

                    Cell statusCell = row.createCell(4);
                    statusCell.setCellValue(status);

                    if ("On-time".equals(status)) statusCell.setCellStyle(greenStyle);
                    else if ("Buffer Late".equals(status)) statusCell.setCellStyle(orangeStyle);
                    else if ("Late".equals(status)) statusCell.setCellStyle(redStyle);

                    userIndex.put(key, index + 1);
                }

                for (int i = 0; i < 5; i++) sheet.autoSizeColumn(i);
            }

            // ===================== SUMMARY SHEET =====================
            Sheet summary = workbook.createSheet("Late Summary");

            Row header = summary.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Late Count");

            Set<String> allUsers = new TreeSet<>();
            for (AttendanceRecord rec : records) {
                allUsers.add(rec.firstName.trim() + "_" + rec.lastName.trim());
            }

            int r = 1;
            for (String userKey : allUsers) {

                Row row = summary.createRow(r++);
                row.createCell(0).setCellValue(userKey.replace("_", " "));

                int count = lateCountMap.getOrDefault(userKey, 0);
                Cell lateCell = row.createCell(1);

                if (count == 0) {
                    lateCell.setCellValue("Always On Time");
                    lateCell.setCellStyle(greenStyle);

                } else if (count <= 2) {
                    lateCell.setCellValue(count + " Times");
                    lateCell.setCellStyle(orangeStyle);

                } else {
                    lateCell.setCellValue(count + " Times");
                    lateCell.setCellStyle(redStyle);
                }
            }

            summary.autoSizeColumn(0);
            summary.autoSizeColumn(1);

            // ===================== SAVE FILE =====================
            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();

            String filePath = folder.getAbsolutePath()
                    + File.separator
                    + "AttendanceRecords_" + System.currentTimeMillis() + ".xlsx";

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

            System.out.println("✔ Excel created successfully: " + filePath);
            return filePath;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
