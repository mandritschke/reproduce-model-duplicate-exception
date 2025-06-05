package reproducer.userstorage;

import org.keycloak.admin.client.Keycloak;

import java.lang.reflect.Constructor;

public interface UserStorage {

    String DEFAULT_PASSWORD = "Passw0rd";

    /**
     * Configure the user storage for the given realm.
     */
    void setUp(Keycloak keycloak, String realmName, String baseUrl);

    /**
     * Create the user.
     */
    void createUser(String username);

    /**
     * Delete the user. Must not fail if the user does not exist.
     */
    void deleteUser(String username);

    static UserStorage forClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (UserStorage.class.isAssignableFrom(clazz)) {
                Constructor<?> constructor = clazz.getConstructor();
                return (UserStorage) constructor.newInstance();
            }
            throw new IllegalArgumentException("Unsupported User Storage: " + className);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unsupported User Storage: " + className, e);
        }
    }
}
