package fr.nordlys.shoploc.accounts.infrastructure.security;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.AuthenticationToken;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenIssuerTest {

    private static final String SECRET = "a-test-secret-of-at-least-32-characters";

    private final JwtTokenIssuer issuer = new JwtTokenIssuer(SECRET, Duration.ofHours(1));

    @Test
    void puts_the_account_id_and_role_in_a_signed_token() {
        Account admin = new Account(42L, "admin@shoploc.fr", "Ada", "Martin", null, null,
                Role.ADMIN, UserStatus.ACTIVE, LocalDateTime.now());

        AuthenticationToken token = issuer.issue(admin);

        SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Jwt jwt = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build()
                .decode(token.value());
        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("ADMIN");
        assertThat(jwt.getExpiresAt()).isEqualTo(token.expiresAt());
    }

    @Test
    void refuses_a_secret_shorter_than_32_characters() {
        assertThatThrownBy(() -> new JwtTokenIssuer("too-short", Duration.ofHours(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
