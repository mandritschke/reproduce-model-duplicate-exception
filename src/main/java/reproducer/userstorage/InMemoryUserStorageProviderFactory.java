package reproducer.userstorage;

import lombok.Getter;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserStorageProviderFactory implements
        UserStorageProviderFactory<InMemoryUserStorageProvider> {

    public static final String PROVIDER_ID = "in-memory";

    @Getter
    private final Set<String> users = ConcurrentHashMap.newKeySet();

    @Override
    public InMemoryUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new InMemoryUserStorageProvider(session, model, users);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void close() {
        // no-op
    }
}
