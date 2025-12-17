package attendanceTests;

import org.testng.Assert;
import org.testng.annotations.Test;
import attendancePages.AccessControl;
import attendanceUtils.EmailSender;
import baseTestFile.BaseTests;

import java.io.File;

public class AccessControlTests extends BaseTests {

    @Test(description = "Verify user can export attendance record and send it via email")
    public void testAccessGrantedRecordsRetrieval() throws InterruptedException {

        AccessControl accesscontrol = new AccessControl(driver);

        accesscontrol.navigateToAccessRecordRetrievalPage();
        accesscontrol.fetchLast30DaysRecords();
        accesscontrol.fetchAttendanceRecord();
        accesscontrol.fetchAllRecords();

        // ✅ GitHub Actions + Local machine safe folder path 
        String folderPath = System.getProperty("user.dir") + "/AttendanceExcels/";

        // Export Excel
        accesscontrol.exportRecordsToExcel(folderPath);

        // Get latest created Excel file
        File exported = getLatestFile(folderPath);

        Assert.assertNotNull(exported, "Exported Excel file not found!");
        Assert.assertTrue(exported.exists(), "Excel file path does not exist!");

        // Send email with attachment
        boolean emailSent = EmailSender.sendEmailWithAttachment(
                "mukesh@peregrine-it.com",
                null,
                "Attendance Report",
                "Please find the attached attendance report.",
                exported.getAbsolutePath()
        );

        Assert.assertTrue(emailSent, "Email was not sent successfully!");
        System.out.println("✅ Test completed successfully.");
    }

    // ------------------ Helper: Get Latest Excel File ------------------
    private File getLatestFile(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xlsx"));

        if (files == null || files.length == 0)
            return null;

        File latest = files[0];
        for (File f : files) {
            if (f.lastModified() > latest.lastModified()) {
                latest = f;
            }
        }
        return latest;
    }
}
