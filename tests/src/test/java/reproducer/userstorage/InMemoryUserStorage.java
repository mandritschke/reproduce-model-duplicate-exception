package reproducer.userstorage;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@NoArgsConstructor
class InMemoryUserStorage implements UserStorage {

    private String baseUrl;
    private String realmName;

    @Override
    public void setUp(Keycloak keycloak, String realmName, String baseUrl) {
        this.baseUrl = baseUrl;
        this.realmName = realmName;

        log.info("Setting up user storage");
        var userStorage = new ComponentRepresentation();
        userStorage.setProviderType("org.keycloak.storage.UserStorageProvider");
        userStorage.setProviderId("in-memory");
        userStorage.setName("in-memory");
        userStorage.setConfig(new MultivaluedHashMap<>(Map.of("cachePolicy", List.of("NO_CACHE"))));
        keycloak.realm(realmName).components().add(userStorage);
    }

    @Override
    public void createUser(String username) {
        log.info("Creating user: {}", username);
        var client = HttpClient.newBuilder()
                .build();
        var registrierungRequest = HttpRequest.newBuilder()
                .uri(getUrl(username))
                .PUT(BodyPublishers.noBody())
                .build();
        try {
            var response = client.send(registrierungRequest, BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void deleteUser(String username) {
        log.info("Deleting user: {}", username);
        var client = HttpClient.newBuilder()
                .build();
        var registrierungRequest = HttpRequest.newBuilder()
                .uri(getUrl(username))
                .DELETE()
                .build();
        try {
            var response = client.send(registrierungRequest, BodyHandlers.ofString());
            assertThat(response.statusCode()).isIn(204);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private URI getUrl(String username) {
        return URI.create(baseUrl
                + "/realms/" + URLEncoder.encode(realmName, StandardCharsets.UTF_8)
                + "/in-memory-users/" + URLEncoder.encode(username, StandardCharsets.UTF_8));
    }
}
