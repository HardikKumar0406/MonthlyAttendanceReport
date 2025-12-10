package attendancePages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Login {
    private WebDriver driver;
    private WebDriverWait wait;

    private final By acceptCookiesBy = By.xpath("(//div[normalize-space()='Accept All'])[1]");

    @FindBy(xpath = "(//input[@placeholder='Account/Email'])[1]")
    private WebElement usernameField;

    @FindBy(xpath = "(//input[@placeholder='Password'])[1]")
    private WebElement passwordField;

    @FindBy(xpath = "(//span[contains(text(),'Login')])[1]")
    private WebElement loginButton;

    @FindBy(xpath = "//div[@id='tab-HCBAccessControl']//span[@title='Access Control'][normalize-space()='Access Control']")
    private WebElement accessControlMenu;

    public Login(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        PageFactory.initElements(driver, this);
    }

    // Accept cookies if present
    public void acceptCookiesIfPresent() {
        try {
            WebElement cookieBtn = wait.until(ExpectedConditions.presenceOfElementLocated(acceptCookiesBy));
            cookieBtn.click();
            System.out.println("Cookies accepted.");
        } catch (Exception e) {
            System.out.println("Accept Cookies not shown or already accepted.");
        }
    }

    // Perform login and go to Access Control
    public void performLogin(String username, String password) throws InterruptedException {

        // Fetch username/password from DEBUG env if set
        String debugUsername = System.getenv("DEBUG_USERNAME");
        String debugPassword = System.getenv("DEBUG_PASSWORD");

        if (debugUsername != null && !debugUsername.isEmpty()) {
            username = debugUsername;
        }
        if (debugPassword != null && !debugPassword.isEmpty()) {
            password = debugPassword;
        }

        System.out.println("Current URL before login: " + driver.getCurrentUrl());
        acceptCookiesIfPresent();

        System.out.println("Trying to enter username");
        wait.until(ExpectedConditions.visibilityOf(usernameField)).sendKeys(username);
      //  System.out.println("Username entered: " + username);

        System.out.println("Trying to enter password");
        wait.until(ExpectedConditions.visibilityOf(passwordField)).sendKeys(password);
 //       System.out.println("Password entered: " + password);

        System.out.println("Trying to click on login");
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
        System.out.println("Clicked on login");
        Thread.sleep(5000);
        System.out.println("Logged in Successfully. Current URL: " + driver.getCurrentUrl());

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[@id='tab-HCBAccessControl']//span[normalize-space()='Access Control']")));

        wait.until(ExpectedConditions.visibilityOf(accessControlMenu));

        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("(//span[@class='path1'])[1]")));
        } catch (Exception e) {
            System.out.println("Loader not found or already gone.");
        }

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", accessControlMenu);
        } catch (Exception e) {
            System.out.println("Access Control menu click failed: " + e.getMessage());
        }
    }
}
