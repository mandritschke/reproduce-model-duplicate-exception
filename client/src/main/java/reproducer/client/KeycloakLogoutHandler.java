package reproducer.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * Behandelt die Abmeldung von Keycloak.
 */
@Slf4j
@RequiredArgsConstructor
public class KeycloakLogoutHandler implements ServerLogoutHandler {

    private final String logoutUri;
    private final WebClient webClient;

    @Override
    public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            return logoutFromKeycloak(oidcUser).then(exchange.getExchange().getSession().flatMap(WebSession::invalidate));
        }
        return Mono.empty();
    }

    private Mono<Void> logoutFromKeycloak(OidcUser user) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(logoutUri)
                .queryParam("id_token_hint", user.getIdToken().getTokenValue());

        return webClient.get()
                .uri(builder.toUriString())
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info("Logout successful"))
                .doOnError(error -> log.error("Logout failed", error))
                .then();
    }

}
