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

    public static class AttendanceRecord {
        public String firstName;
        public String lastName;
        public String accessTime;   // Time received from API (UTC)
        public String checkType;

        public AttendanceRecord(String firstName, String lastName, String accessTime, String checkType) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.accessTime = accessTime; // UTC timestamp
            this.checkType = checkType;
        }
    }

    // --- FORMATTERS ---
    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); // API format (UTC)

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter OUTPUT_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // --- IST Conversion Helper ---
    private static LocalDateTime convertToIST(String accessTime) {
        LocalDateTime utcDT = LocalDateTime.parse(accessTime, INPUT_FORMAT);
        ZonedDateTime utcZdt = utcDT.atZone(ZoneId.of("UTC"));
        ZonedDateTime istZdt = utcZdt.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        return istZdt.toLocalDateTime();
    }

    // --- EXPORT METHOD ---
    public static String exportAttendance(List<AttendanceRecord> records, String folderPath) {

        try (Workbook workbook = new XSSFWorkbook()) {

            // 🎨 FONT STYLES (No background, only text color)
            Font greenFont = workbook.createFont();
            greenFont.setColor(IndexedColors.GREEN.getIndex());
            greenFont.setBold(true);

            Font orangeFont = workbook.createFont();
            orangeFont.setColor(IndexedColors.ORANGE.getIndex());
            orangeFont.setBold(true);

            Font redFont = workbook.createFont();
            redFont.setColor(IndexedColors.RED.getIndex());
            redFont.setBold(true);

            Font defaultFont = workbook.createFont();
            defaultFont.setBold(false);

            CellStyle greenStyle = workbook.createCellStyle();
            greenStyle.setFont(greenFont);

            CellStyle orangeStyle = workbook.createCellStyle();
            orangeStyle.setFont(orangeFont);

            CellStyle redStyle = workbook.createCellStyle();
            redStyle.setFont(redFont);

            CellStyle defaultStyle = workbook.createCellStyle();
            defaultStyle.setFont(defaultFont);

            // --- Group by IST Date ---
            Map<String, List<AttendanceRecord>> groupedByDate = new TreeMap<>();

            for (AttendanceRecord rec : records) {
                LocalDateTime dtIST = convertToIST(rec.accessTime);
                String dateKey = dtIST.toLocalDate().format(DATE_FORMAT);
                groupedByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(rec);
            }

            // --- Create Sheets per Date ---
            for (String date : groupedByDate.keySet()) {

                Sheet sheet = workbook.createSheet(date);

                // Header
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("First Name");
                header.createCell(1).setCellValue("Last Name");
                header.createCell(2).setCellValue("Access Time (IST)");
                header.createCell(3).setCellValue("Tag");
                header.createCell(4).setCellValue("Attendance Status");

                List<AttendanceRecord> dayRecords = groupedByDate.get(date);

                // Sort ISO IST times
                dayRecords.sort(Comparator.comparing(r -> convertToIST(r.accessTime)));

                // Per user grouping
                Map<String, List<AttendanceRecord>> userSortedMap = new HashMap<>();

                for (AttendanceRecord rec : dayRecords) {
                    String key = rec.firstName.trim() + "_" + rec.lastName.trim();
                    userSortedMap.computeIfAbsent(key, k -> new ArrayList<>()).add(rec);
                }

                // Sort each user's list by IST time
                for (List<AttendanceRecord> list : userSortedMap.values()) {
                    list.sort(Comparator.comparing(r -> convertToIST(r.accessTime)));
                }

                Map<String, Integer> userIndexTracker = new HashMap<>();
                int rowIndex = 1;

                // --- Write Rows ---
                for (AttendanceRecord rec : dayRecords) {

                    String key = rec.firstName.trim() + "_" + rec.lastName.trim();
                    List<AttendanceRecord> sortedList = userSortedMap.get(key);

                    int currentIndex = userIndexTracker.getOrDefault(key, 0);
                    AttendanceRecord sortedRec = sortedList.get(currentIndex);

                    LocalDateTime dtIST = convertToIST(sortedRec.accessTime);

                    String tag = "";
                    String status = "";

                    // TAG / STATUS Logic
                    if (currentIndex == 0) {  // First entry of the day = Clock-In
                        tag = "Clock-in";

                        LocalTime t = dtIST.toLocalTime();
                        if (!t.isAfter(LocalTime.of(10, 0)))
                            status = "On-time";
                        else if (!t.isAfter(LocalTime.of(10, 15)))
                            status = "Buffer Late";
                        else
                            status = "Late";

                    } else { // Any entry after first
                        if ("Check-Out".equalsIgnoreCase(sortedRec.checkType)) {
                            tag = "Clock-out";
                        } else {
                            tag = "Break";
                        }
                    }

                    // Write Row
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(rec.firstName);
                    row.createCell(1).setCellValue(rec.lastName);
                    row.createCell(2).setCellValue(dtIST.format(OUTPUT_DATETIME));
                    row.createCell(3).setCellValue(tag);

                    // Status with color
                    Cell statusCell = row.createCell(4);
                    statusCell.setCellValue(status);

                    switch (status) {
                        case "On-time":
                            statusCell.setCellStyle(greenStyle);
                            break;
                        case "Buffer Late":
                            statusCell.setCellStyle(orangeStyle);
                            break;
                        case "Late":
                            statusCell.setCellStyle(redStyle);
                            break;
                        default:
                            statusCell.setCellStyle(defaultStyle);
                    }

                    userIndexTracker.put(key, currentIndex + 1);
                }

                // Auto-size
                for (int col = 0; col < 5; col++) {
                    sheet.autoSizeColumn(col);
                }
            }

            // --- Ensure Folder Exists ---
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // File Name
            String fileName = "AttendanceRecords_" + System.currentTimeMillis() + ".xlsx";
            String fullPath = folder.getAbsolutePath() + File.separator + fileName;

            // Save file
            try (FileOutputStream out = new FileOutputStream(fullPath)) {
                workbook.write(out);
            }

            System.out.println("✔ Excel created successfully in IST: " + fullPath);
            return fullPath;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
