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

    // ===================== LOADER =====================
    private static final By LOADER =
            By.cssSelector("div.el-loading-mask");

    // ===================== ELEMENTS =====================
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

    // ===================== CONSTRUCTOR =====================
    public AccessRecordNavigation(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        PageFactory.initElements(driver, this);
    }

    // ===================== COMMON UTILS =====================
    private void waitForLoaderToDisappear() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADER));
    }

    private void safeClick(WebElement element) {
        waitForLoaderToDisappear();
        wait.until(ExpectedConditions.visibilityOf(element));
        wait.until(ExpectedConditions.elementToBeClickable(element));

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", element);

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", element);
    }

    // ===================== NAVIGATION =====================
    public void navigateToAccessRecordRetrievalPage() {

        // Initial loader
        waitForLoaderToDisappear();

        // OK popup
        safeClick(clickOnOK);

        // Refresh
        waitForLoaderToDisappear();
        safeClick(clickOnRefreshIcon);

        // Navigate menu
        safeClick(clickOnSearch);
        safeClick(clickOnAccessRecordRetrieval);
    }

    // ===================== FETCH RECORDS =====================
    public void fetchAttendanceRecord() {

        waitForLoaderToDisappear();

        safeClick(clickOnMore);
        safeClick(clickOnChooseAccessStatus);
        safeClick(selectAccessGrantedOption);

        safeClick(searchButton);

        waitForLoaderToDisappear();

        safeClick(clickOnDropDownToChangePagination);
        safeClick(chooseRowTotalToTen);

        System.out.println("✅ Attendance records fetched successfully.");
    }
}
