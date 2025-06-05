package reproducer.userstorage;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import org.keycloak.storage.UserStorageProvider;

import java.util.Set;

@Slf4j
public class InMemoryUserServiceEndpoint implements RealmResourceProviderFactory, RealmResourceProvider {

    private Set<String> users;

    @Override
    public String getId() {
        return "in-memory-users";
    }

    @Override
    public void init(Scope config) {
        // no-op
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        users = ((InMemoryUserStorageProviderFactory) factory.getProviderFactory(UserStorageProvider.class,
                InMemoryUserStorageProviderFactory.PROVIDER_ID)).getUsers();
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    public InMemoryUserServiceEndpoint getResource() {
        return this;
    }

    @DELETE
    @Path("/{username}")
    public Response delete(@PathParam("username") String username) {
        boolean deleted = users.remove(username);
        if (deleted) {
            log.info("Deleted user: {}", username);
        }
        return Response.noContent().build();
    }

    @PUT
    @Path("/{username}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response put(@PathParam("username") String username) {
        boolean created = users.add(username);
        if (created) {
            log.info("Created user: {}", username);
        }
        return Response.ok("").build();
    }

    @Override
    public void close() {
        // no-op
    }
}
