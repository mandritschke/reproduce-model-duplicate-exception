package reproducer.client;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.openqa.selenium.WebDriver;
import reproducer.selenium.model.AccountClientPage;
import reproducer.selenium.model.LoginPage;

@Slf4j
@NoArgsConstructor
class AccountClient implements Client {

    private String clientUrl;

    @Override
    public void setUp(Keycloak keycloak, String realmName, String baseUrl) {
        clientUrl = baseUrl + String.format("/realms/%s/account/", realmName);
    }

    @Override
    public LoginPage goToLoginPage(WebDriver driver) {
        log.info("Navigating to {}", clientUrl);
        driver.get(clientUrl);
        return new LoginPage(driver, AccountClientPage::new);
    }

}
