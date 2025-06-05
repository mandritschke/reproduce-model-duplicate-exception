package reproducer.userstorage;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@NoArgsConstructor
class InternalUserStorage implements UserStorage {

    private Keycloak keycloak;
    private String realmName;

    @Override
    public void setUp(Keycloak keycloak, String realmName, String baseUrl) {
        this.keycloak = keycloak;
        this.realmName = realmName;
    }

    @Override
    public void createUser(String username) {
        log.info("Creating user: {}", username);
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(username + "@example.com");
        user.setEnabled(true);

        var response = keycloak.realm(realmName).users().create(user);
        assertThat(response.getStatus()).as("User created, status code").isEqualTo(201);
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(DEFAULT_PASSWORD);
        passwordCred.setTemporary(false);

        keycloak.realm(realmName).users().get(userId).resetPassword(passwordCred);
    }

    @Override
    public void deleteUser(String username) {
        log.info("Deleting user: {}", username);
        List<UserRepresentation> searchResult = keycloak.realm(realmName).users().searchByUsername(username, true);
        if (searchResult.isEmpty()) {
            return;
        }

        searchResult.forEach(user -> {
            var response = keycloak.realm(realmName).users().delete(user.getId());
            assertThat(response.getStatus()).as("User deleted, status code").isEqualTo(204);
        });
    }
}
