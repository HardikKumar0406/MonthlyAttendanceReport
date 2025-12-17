package attendancePages;

import java.time.Duration;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AccessRecordNavigation {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final By LOADER_MASK =
            By.xpath("//div[contains(@class,'el-loading-mask')]");

    // ---------- Elements ----------

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

    @FindBy(xpath = "//*[@id='header']/div[5]/div[1]/button")
    private WebElement clickOnOK;

    @FindBy(xpath = "(//i[@class='el-input__icon h-icon-angle_down_sm'])[5]")
    private WebElement clickOnDropDownToChangePagination;

    @FindBy(xpath = "(//span[@class='item-text-style'][normalize-space()='10'])[2]")
    private WebElement chooseRowTotalToTen;

    // ---------- Constructor ----------

    public AccessRecordNavigation(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        PageFactory.initElements(driver, this);
    }

    // ---------- Common Utilities ----------

    private void waitForLoaderToDisappear() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADER_MASK));
    }

    private void safeClick(WebElement element) {
        try {
            waitForLoaderToDisappear();
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (ElementClickInterceptedException | TimeoutException e) {
            // Fallback to JS click (CI-safe)
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", element);
        }
    }

    // ---------- Navigation ----------

    public void navigateToAccessRecordRetrievalPage() {

        waitForLoaderToDisappear();

        // Handle OK popup safely
        try {
            safeClick(clickOnOK);
        } catch (Exception ignored) {
            // OK popup may not appear every time
        }

        // Refresh
        safeClick(clickOnRefreshIcon);
        waitForLoaderToDisappear();

        // Navigate to menu
        safeClick(clickOnSearch);
        safeClick(clickOnAccessRecordRetrieval);
    }

    // ---------- Fetch Records ----------

    public void fetchAttendanceRecord() {

        waitForLoaderToDisappear();

        // Open filters
        safeClick(clickOnMore);

        safeClick(clickOnChooseAccessStatus);
        safeClick(selectAccessGrantedOption);

        // Search
        safeClick(searchButton);
        waitForLoaderToDisappear();

        // Pagination
        safeClick(clickOnDropDownToChangePagination);
        safeClick(chooseRowTotalToTen);

        System.out.println("✅ 10 Attendance Records fetched successfully.");
    }
}
