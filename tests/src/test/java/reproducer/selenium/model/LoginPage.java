package reproducer.selenium.model;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Page model of Keycloak's login page.
 */
@Slf4j
public class LoginPage {

    private final WebDriver driver;

    private final By userNameInputSelector = By.cssSelector("input#username");
    private final By passwordInputSelector = By.cssSelector("input#password");
    private final By loginButtonSelector = By.cssSelector("#kc-login");

    private final Function<WebDriver, ClientPage> clientPageFunction;

    public LoginPage(WebDriver driver, Function<WebDriver, ClientPage> clientPageFunction) {
        this.driver = driver;
        this.clientPageFunction = clientPageFunction;

        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(5))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NoSuchElementException.class)
                .withMessage("Waiting for login button")
                .until(d -> d.findElement(loginButtonSelector));
    }

    /**
     * Attempt to login with given credentials.
     * <p>
     * Expects Keycloak's embedded account client.
     */
    public ClientPage loginToAccountClient(String username, String password) {
        log.info("Login to account client as {}", username);
        WebElement userNameInput = driver.findElement(userNameInputSelector);
        WebElement passwordInput = driver.findElement(passwordInputSelector);
        assertIsDisplayedAndEnabled(userNameInput, "username input");
        assertIsDisplayedAndEnabled(passwordInput, "password input");
        log.info("Entering username");
        userNameInput.clear();
        userNameInput.sendKeys(username);
        log.info("Entering password");
        passwordInput.clear();
        passwordInput.sendKeys(password);
        WebElement loginButton = driver.findElement(loginButtonSelector);
        assertIsDisplayedAndEnabled(loginButton, "login button");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        log.info("Clicking login button");
        loginButton.click();
        return clientPageFunction.apply(driver);
    }

    private void assertIsDisplayedAndEnabled(WebElement element, String description) {
        assertThat(element).as(description).satisfies(button -> {
            assertThat(button.isDisplayed()).as("is displayed").isTrue();
            assertThat(button.isEnabled()).as("is enabled").isTrue();
        });
    }
}
