package reproducer.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.openqa.selenium.WebDriver;
import reproducer.client.Client;
import reproducer.selenium.extension.SeleniumExtension;
import reproducer.selenium.model.ClientPage;
import reproducer.selenium.model.LoginPage;
import reproducer.userstorage.UserStorage;

@Slf4j
@ExtendWith(SeleniumExtension.class)
class LoginIT {

    static final String CLIENT = System.getenv().getOrDefault("CLIENT", "reproducer.client.SpringClient");
    static final String CLIENT_URL = System.getenv().getOrDefault("CLIENT_URL", "http://localhost:8090");
    static final String KC_HOSTNAME_ADMIN = System.getenv().getOrDefault("KC_HOSTNAME_ADMIN", "http://localhost:8080");
    static final String KC_BOOTSTRAP_ADMIN_USERNAME = System.getenv().getOrDefault("KC_BOOTSTRAP_ADMIN_USERNAME", "admin");
    static final String KC_BOOTSTRAP_ADMIN_PASSWORD = System.getenv().getOrDefault("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin");
    static final String USER_STORAGE = System.getenv().getOrDefault("USER_STORAGE", "reproducer.userstorage.InMemoryUserStorage");

    static final String REALM_NAME = "reproducer";
    static final String USER_1 = "testuser1";
    static final String USER_2 = "testuser2";

    static Client client;
    static Keycloak keycloak;
    static UserStorage userStorage;

    @BeforeAll
    static void setUpRealm() {
        log.info("""
                Test Environment

                CLIENT={}
                CLIENT_URL={}
                KC_HOSTNAME_ADMIN={}
                KC_BOOTSTRAP_ADMIN_USERNAME={}
                KC_BOOTSTRAP_ADMIN_PASSWORD={}
                USER_STORAGE={}
                """, CLIENT, CLIENT_URL, KC_HOSTNAME_ADMIN, KC_BOOTSTRAP_ADMIN_USERNAME, KC_BOOTSTRAP_ADMIN_PASSWORD, USER_STORAGE);

        keycloak = Keycloak.getInstance(KC_HOSTNAME_ADMIN, "master", KC_BOOTSTRAP_ADMIN_USERNAME, KC_BOOTSTRAP_ADMIN_PASSWORD, "admin-cli");

        if (keycloak.realms().findAll().stream().anyMatch(realm -> REALM_NAME.equals(realm.getRealm()))) {
            log.info("Deleting existing test realm for a fresh start");
            keycloak.realm(REALM_NAME).remove();
        }

        log.info("Creating new test realm");
        var realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(REALM_NAME);
        realmRepresentation.setEnabled(true);
        keycloak.realms().create(realmRepresentation);

        var verifyProfileAction = keycloak.realm(REALM_NAME).flows().getRequiredAction("VERIFY_PROFILE");
        verifyProfileAction.setEnabled(false);
        keycloak.realm(REALM_NAME).flows().updateRequiredAction("VERIFY_PROFILE", verifyProfileAction);

        userStorage = UserStorage.forClass(USER_STORAGE);
        userStorage.setUp(keycloak, REALM_NAME, KC_HOSTNAME_ADMIN);
        userStorage.deleteUser(USER_1);
        userStorage.deleteUser(USER_2);

        client = Client.forClass(CLIENT);
        client.setUp(keycloak, REALM_NAME, CLIENT_URL);
    }

    @Test
    void testAccountAdminClient(WebDriver driver) throws Exception {
        // at least two passes are necessary to trigger the issue
        for (int i = 0; i < 2; i++) {
            LoginPage loginPage = client.goToLoginPage(driver);

            // 1st user - inconsistent delete before logout
            userStorage.createUser(USER_1);
            ClientPage clientPage = loginPage.loginToAccountClient(USER_1, UserStorage.DEFAULT_PASSWORD);
            userStorage.deleteUser(USER_1);
            loginPage = clientPage.logout();

            // 2nd user - consistent delete after logout
            userStorage.createUser(USER_2);
            clientPage = loginPage.loginToAccountClient(USER_2, UserStorage.DEFAULT_PASSWORD); // 💥 failure on 2nd pass
            loginPage = clientPage.logout();
            userStorage.deleteUser(USER_2);
        }
    }
}
