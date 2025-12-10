package attendancePages;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import attendanceUtils.ExcelExporter;

public class AccessControl {

    private WebDriver driver;
    private WebDriverWait wait;

    public LocalDate selectedStartDate;
    public LocalDate selectedEndDate;
    public LocalDate selectedReportDate;

    // STORE RAW RECORDS HERE
    public List<ExcelExporter.AttendanceRecord> allRecords = new ArrayList<>();

    public AccessControl(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }

    // ------------------ ELEMENTS ------------------------
    @FindBy(xpath = "(//i[@class='el-submenu__icon-arrow h-icon-angle_down_sm'])[2]")
    private WebElement clickOnSearch;

    @FindBy(xpath = "(//span[@id='s_menu_access_personnelsearch'])[1]")
    private WebElement clickOnAccessRecordRetrieval;

    @FindBy(xpath = "(//span[@class='pointer'])[1]")
    private WebElement clickOnMore;

    @FindBy(xpath = "(//input[@placeholder='Please Select'])[3]")
    private WebElement clickOnChooseAccessStatus;

    @FindBy(xpath = "//span[@class='item-text-style' and text()='Access Granted']")
    private WebElement selectAccessGrantedOption;

    @FindBy(xpath = "(//div[@class='el-button-slot-wrapper'][normalize-space()='Search'])[1]")
    private WebElement searchButton;

    @FindBy(xpath = "(//i[@class='h-icon-refresh'])[1]")
    private WebElement clickOnRefreshIcon;

    @FindBy(xpath = "//*[@id=\"header\"]/div[5]/div[1]/button")
    private WebElement clickOnOK;

    @FindBy(xpath = "(//button[@title='Custom Column Item'])[1]")
    private WebElement getFilteredData;

    @FindBy(xpath = "(//span[@class='el-checkbox__inner'])[4]")
    private WebElement clickOnCheckbox;

    @FindBy(xpath = "(//div[@class='drawer-icon'])[1]")
    private WebElement clickOnDrawerIcon;

    @FindBy(xpath = "(//i[@class='el-input__icon h-icon-angle_down_sm'])[5]")
    private WebElement clickOnDropDownToChangePagination;

    @FindBy(xpath = "(//span[@class='item-text-style'][normalize-space()='10'])[2]")
    private WebElement chooseRowTotalToTen;

    @FindBy(xpath = "(//input[@placeholder='Please Select'])[2]")
    private WebElement chooseTime;

    @FindBy(xpath = "(//span[normalize-space()='Yesterday'])[1]")
    private WebElement clickOnYesterday;

    @FindBy(xpath = "(//span[normalize-space()='Last 7 Days'])[1]")
    private WebElement clickOnLast30Days;

    @FindBy(xpath = "(//span[normalize-space()='Custom Time Interval'])[1]")
    private WebElement chooseCustomDate;

    @FindBy(xpath = "(//span[@class='cell'][normalize-space()='1'])[1]")
    private WebElement selectStartDate;

    @FindBy(xpath = "(//span[contains(text(),'30')])[5]")
    private WebElement selectEndDate;

    @FindBy(xpath = "(//button[contains(text(),'OK')])[1]")
    private WebElement clickOnOk;

    @FindBy(xpath = "/html/body/div[6]/div[2]/div[1]/span[1]/span[1]")
    private WebElement startDateSpan;

    @FindBy(xpath = "/html/body/div[6]/div[2]/div[1]/span[4]")
    private WebElement endDateSpan;

    @FindBy(xpath = "(//span[@class='el-checkbox__inner'])[9]")
    private WebElement disableSkinSurfaceTemperature;

    @FindBy(xpath = "(//span[@class='el-checkbox__inner'])[10]")
    private WebElement disableWearingMaskOrNot;

    @FindBy(xpath = "(//span[@class='el-checkbox__inner'])[11]")
    private WebElement disableCardNumber;

    @FindBy(xpath = "(//span[@class='el-checkbox__inner'])[12]")
    private WebElement disableDepartment;

    @FindBy(xpath = "(//span[@class='el-checkbox__inner'])[17]")
    private WebElement disableAuthenticationType;


    // ========================= DATE SELECTION =============================

    public void fetchYesterdayRecords() {
        chooseTime.click();
        clickOnYesterday.click();

        selectedReportDate = LocalDate.now().minusDays(1);
        selectedStartDate = selectedEndDate = selectedReportDate;
    }

    public void fetchLast30DaysRecords() throws InterruptedException {
        chooseTime.click();
        Thread.sleep(1000);        
        clickOnLast30Days.click();

        selectedStartDate = LocalDate.now().minusDays(30);
        selectedEndDate = LocalDate.now();
        selectedReportDate = selectedStartDate;
    }

    public void fetchTodaysRecords() {
        selectedReportDate = LocalDate.now();
        selectedStartDate = selectedEndDate = selectedReportDate;
    }

    // ========================= NAVIGATION =============================

    public void navigateToAccessRecordRetrievalPage() throws InterruptedException {
        Thread.sleep(3000);
        clickOnOK.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".el-loading-mask")));

        Thread.sleep(2000);
        clickOnRefreshIcon.click();
        Thread.sleep(2000);

        clickOnSearch.click();
        clickOnAccessRecordRetrieval.click();
    }

    // ========================= APPLY FILTERS =============================

    public void fetchAttendanceRecord() throws InterruptedException {
    	Thread.sleep(1000);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", clickOnMore);
        Thread.sleep(1000);
        clickOnChooseAccessStatus.click();
        Thread.sleep(1000);
        selectAccessGrantedOption.click();
        Thread.sleep(1000);
        searchButton.click();
        Thread.sleep(3000);
        Thread.sleep(1000);
        getFilteredData.click();
        Thread.sleep(1000);
        clickOnCheckbox.click();

        disableSkinSurfaceTemperature.click();
        disableAuthenticationType.click();
        disableCardNumber.click();
        disableDepartment.click();
        disableWearingMaskOrNot.click();

        clickOnDrawerIcon.click();
        Thread.sleep(2000);

        clickOnDropDownToChangePagination.click();
        Thread.sleep(1000);
        chooseRowTotalToTen.click();

        Thread.sleep(2000);
        System.out.println("Filters applied. Fetching records...");
    }

    // ========================= FETCH ALL RECORDS =============================

    public void fetchAllRecords() throws InterruptedException {

        allRecords.clear(); // RESET LIST
        int pageNumber = 1;

        while (true) {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".el-table__body tbody tr.el-table__row")));

            List<WebElement> rows =
                    driver.findElements(By.cssSelector(".el-table__body tbody tr.el-table__row"));

            for (WebElement row : rows) {
                List<WebElement> cells = row.findElements(By.cssSelector("td .cell"));

                if (cells.size() < 8) continue;

                String firstName = cells.get(1).getText().trim();
                String lastName = cells.get(2).getText().trim();
                String accessTime = cells.get(4).getText().trim();
                String checkType = cells.get(8).getText().trim();

                allRecords.add(new ExcelExporter.AttendanceRecord(firstName, lastName, accessTime, checkType));
            }

            // Check pagination
            WebElement nextButton;
            try {
                nextButton = driver.findElement(By.xpath("(//li[@class='h-icon-angle_right'])[1]"));
            } catch (Exception e) {
                break;
            }

            if (nextButton.getAttribute("class").contains("is-disabled")) break;

            nextButton.click();
            Thread.sleep(1500);
            pageNumber++;
        }

        System.out.println("Total attendance records fetched: " + allRecords.size());
    }

    // ========================= EXPORT TO EXCEL =============================

    public void exportRecordsToExcel(String folderPath) {
        ExcelExporter.exportAttendance(allRecords, folderPath);
    }
}
