package reproducer.client;

import org.keycloak.admin.client.Keycloak;
import org.openqa.selenium.WebDriver;
import reproducer.selenium.model.LoginPage;

import java.lang.reflect.Constructor;

public interface Client {

    /**
     * Configure the client for the given realm.
     */
    void setUp(Keycloak keycloak, String realmName, String baseUrl);

    /**
     * Access the client via Selenium and be redirected to the Login Page.
     */
    LoginPage goToLoginPage(WebDriver driver);

    static Client forClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (Client.class.isAssignableFrom(clazz)) {
                Constructor<?> constructor = clazz.getConstructor();
                return (Client) constructor.newInstance();
            }
            throw new IllegalArgumentException("Unsupported Client: " + className);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unsupported Client: " + className, e);
        }
    }
}
