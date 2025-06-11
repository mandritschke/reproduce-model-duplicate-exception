package reproducer.selenium.model;

/**
 * Interface for a simple client page.
 */
public interface ClientPage {

    /**
     * Triggers the logout from the client and returns to the login page.
     */
    LoginPage logout();

}