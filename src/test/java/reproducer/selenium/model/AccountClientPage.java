package reproducer.selenium.model;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;

@Slf4j
public class AccountClientPage {

    private final WebDriver driver;

    private final By menuButtonSelector = By.cssSelector("button.pf-v5-c-menu-toggle");
    private final By logoutMenuItemSelector = By.cssSelector("button.pf-v5-c-menu__item");

    public AccountClientPage(WebDriver driver) {
        this.driver = driver;

        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(5))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NoSuchElementException.class)
                .withMessage("Waiting for account client page to load")
                .until(d -> d.findElement(menuButtonSelector));

        log.info("Login successful");
    }

    /**
     * Triggers the logout from the client and returns to the login page.
     */
    public LoginPage logout() {
        log.info("Logout");
        driver.findElement(menuButtonSelector).click();
        driver.findElement(logoutMenuItemSelector).click();
        return new LoginPage(driver);
    }
}
