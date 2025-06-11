package reproducer.client;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.openqa.selenium.WebDriver;
import reproducer.selenium.model.LoginPage;
import reproducer.selenium.model.SpringClientPage;

import java.util.List;

@Slf4j
@NoArgsConstructor
class SpringClient implements Client {

    private String clientUrl;

    @Override
    public void setUp(Keycloak keycloak, String realmName, String baseUrl) {
        clientUrl = baseUrl;

        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId("spring-client");
        clientRepresentation.setSecret("spring-secret");
        clientRepresentation.setRedirectUris(List.of(baseUrl + "/*"));
        keycloak.realm(realmName).clients().create(clientRepresentation);
    }

    @Override
    public LoginPage goToLoginPage(WebDriver driver) {
        log.info("Navigating to {}", clientUrl);
        driver.get(clientUrl);
        return new LoginPage(driver, SpringClientPage::new);
    }

}
