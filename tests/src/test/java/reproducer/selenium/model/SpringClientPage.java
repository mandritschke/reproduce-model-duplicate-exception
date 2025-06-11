package reproducer.selenium.model;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;

/**
 * Page model for the external Spring client.
 */
@Slf4j
public class SpringClientPage implements ClientPage {

    private final WebDriver driver;

    private final By logoutMenuItemSelector = By.cssSelector("button#logoutBtn");

    public SpringClientPage(WebDriver driver) {
            this.driver = driver;

            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(5))
                    .pollingEvery(Duration.ofSeconds(1))
                    .ignoring(NoSuchElementException.class)
                    .withMessage("Waiting for account client page to load")
                    .until(d -> d.findElement(logoutMenuItemSelector));

            log.info("Login successful");
        }

    @Override
    public LoginPage logout() {
        log.info("Logout");
        driver.findElement(logoutMenuItemSelector).click();
        return new LoginPage(driver, SpringClientPage::new);
    }
}
