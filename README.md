# Reproducer for a ModelDuplicateException

This reproducer describes what is probably a session handling problem during authentication in Keycloak.

Keycloak Issues
* [#39923](https://github.com/keycloak/keycloak/issues/39923)
* [#40398](https://github.com/keycloak/keycloak/issues/40398)

## Quick start

1. Build the user storage provider
```
mvn -f userstorage package
```
2. Drop the .jar into Keycloak's `providers` folder and boot up Keycloak, e.g. using Docker
```
docker run \
    --mount type=bind,source=./userstorage/target/reproducer.jar,target=/opt/keycloak/providers/reproducer.jar \
    --name reproducer \
    -p 8080:8080 \
    -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
    -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
    quay.io/keycloak/keycloak:26.2.4 start-dev
```
3. Start the client
```
mvn -f client spring-boot:run
```
4. Run the Selenium Test
```
mvn -f tests verify
```
Observe the test failing and the container log showing a runtime exception
```
2025-06-05 10:18:51,828 ERROR [org.keycloak.services.error.KeycloakErrorHandler] (executor-thread-4) Uncaught server error: java.lang.RuntimeException: unable to complete the session updates
        at org.keycloak.models.sessions.infinispan.changes.JpaChangesPerformer.applyChanges(JpaChangesPerformer.java:112)
        at java.base/java.lang.Iterable.forEach(Iterable.java:75)
        at org.keycloak.models.sessions.infinispan.changes.PersistentSessionsChangelogBasedTransaction.commitImpl(PersistentSessionsChangelogBasedTransaction.java:222)
        at org.keycloak.models.AbstractKeycloakTransaction.commit(AbstractKeycloakTransaction.java:46)
        at org.keycloak.services.DefaultKeycloakTransactionManager.lambda$commitWithTracing$0(DefaultKeycloakTransactionManager.java:169)
...
        Suppressed: org.keycloak.models.ModelDuplicateException: Duplicate resource error
```
The concrete error message depends on the configured database.

## Notes

- The test creates a new realm and tries to log in and out of the client several times. It switches between two user accounts. In one occurrence the user is even deleted while they are still signed in. This works fine in the first iteration of the test, but breaks in the second iteration.
- User Storage Cache needs to be disabled; otherwise the test will succeed, because Keycloak doesn't immediately write-through to the database.
- You may want to compare the test results to Keycloak's internal account client:
```
CLIENT=reproducer.client.AccountClient \
CLIENT_URL=http://localhost:8080 \
    mvn -f tests verify
```
This has already been addressed via [#40352](https://github.com/keycloak/keycloak/pull/40352).
- You may want to compare the test results to Keycloak's internal user storage:
```
USER_STORAGE=reproducer.userstorage.InternalUserStorage \
    mvn -f tests verify
```
The test will fail there too at the same stage, but there is no `ModelDuplicateException` and the request fails
when the external client tries to fetch the access token for the supplied authorization code.

