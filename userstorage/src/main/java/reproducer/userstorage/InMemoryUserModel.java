package reproducer.userstorage;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.adapter.AbstractUserAdapter;

public class InMemoryUserModel extends AbstractUserAdapter {

    public static final String DEFAULT_PASSWORD = "Passw0rd";

    private final String username;

    public InMemoryUserModel(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel, String username) {
        super(session, realm, storageProviderModel);
        this.username = username;
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return new UserCredentialManager(session, realm, this);
    }

    @Override
    public String getUsername() {
        return username;
    }
}
