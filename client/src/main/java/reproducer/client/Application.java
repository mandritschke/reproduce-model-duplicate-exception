package reproducer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@Slf4j
@SpringBootApplication
public class Application {

    static final String PROTECTED_RESOURCE = "/";
    static final String PROTECTED_RESOURCE_ALIAS = "/index.html";
    static final String LOGOUT_ENDPOINT = "/logout";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    WebClient webClient() {
        return WebClient.builder().build();
    }

    /**
     * Konfiguriert die SecurityWebFilterChain für die Anwendung.
     */
    @Bean
    SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http,
            WebClient webClient,
            @Value("${keycloak.logout-uri}") String keycloakLogoutUrl) {
        return http
                .csrf(CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(PROTECTED_RESOURCE, PROTECTED_RESOURCE_ALIAS).authenticated()
                        .anyExchange().permitAll())
                .oauth2Login(oauth2Login -> oauth2Login
                        .authenticationFailureHandler(new LoggingRedirectServerAuthenticationFailureHandler(PROTECTED_RESOURCE))
                        .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler(PROTECTED_RESOURCE)))
                .logout(logout -> logout
                        .logoutUrl(LOGOUT_ENDPOINT)
                        .logoutHandler(new KeycloakLogoutHandler(keycloakLogoutUrl, webClient))
                        .logoutSuccessHandler(logoutSuccessHandler()))
                .build();
    }

    private RedirectServerLogoutSuccessHandler logoutSuccessHandler() {
        RedirectServerLogoutSuccessHandler logoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
        logoutSuccessHandler.setLogoutSuccessUrl(URI.create(PROTECTED_RESOURCE));
        return logoutSuccessHandler;
    }
}
