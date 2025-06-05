package reproducer.userstorage;

import lombok.RequiredArgsConstructor;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

import java.util.Set;

@RequiredArgsConstructor
public class InMemoryUserStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator {

    private final KeycloakSession session;
    private final ComponentModel storageComponentModel;
    private final Set<String> users;

    @Override
    public void close() {
        // no-op
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        return users.stream().filter(user -> user.equals(StorageId.externalId(id)))
                .findFirst()
                .map(user -> new InMemoryUserModel(session, realm, storageComponentModel, user))
                .orElse(null);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        return users.stream().filter(user -> user.equals(username))
                .findFirst()
                .map(user -> new InMemoryUserModel(session, realm, storageComponentModel, user))
                .orElse(null);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        // unsupported
        return null;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (isConfiguredFor(realm, user, credentialInput.getType())) {
            return InMemoryUserModel.DEFAULT_PASSWORD.equals(credentialInput.getChallengeResponse());
        }
        return false;
    }
}
