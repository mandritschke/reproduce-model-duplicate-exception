package reproducer.selenium.extension;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.Optional;

public class SeleniumExtension implements AfterAllCallback, ParameterResolver {

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        getCurrentWebDriver(context).ifPresent(WebDriver::quit);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();
        return WebDriver.class.equals(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();
        if (WebDriver.class.equals(type)) {
            return getCurrentOrCreateWebDriver(extensionContext);
        }
        return null;

    }

    private WebDriver getCurrentOrCreateWebDriver(ExtensionContext extensionContext) {
        return extensionContext.getStore(Namespace.create(SeleniumExtension.class.getName()))
                .getOrComputeIfAbsent(WebDriver.class.getName(), key -> this.createNewWebDriver(), WebDriver.class);
    }

    private Optional<WebDriver> getCurrentWebDriver(ExtensionContext extensionContext) {
        return Optional.ofNullable(extensionContext.getStore(Namespace.create(SeleniumExtension.class.getName()))
                .get(WebDriver.class.getName(), WebDriver.class));
    }

    private WebDriver createNewWebDriver() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setAcceptInsecureCerts(true);

        return WebDriverManager.firefoxdriver()
                .capabilities(firefoxOptions)
                .create();
    }

}
