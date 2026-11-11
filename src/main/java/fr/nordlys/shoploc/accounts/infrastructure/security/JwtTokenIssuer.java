package fr.nordlys.shoploc.accounts.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.AuthenticationToken;
import fr.nordlys.shoploc.accounts.domain.port.TokenIssuer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    private final JwtEncoder encoder;
    private final Duration validity;

    public JwtTokenIssuer(@Value("${shoploc.security.jwt.secret}") String secret,
                          @Value("${shoploc.security.jwt.validity:1h}") Duration validity) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 characters");
        }
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.validity = validity;
    }

    @Override
    public AuthenticationToken issue(Account account) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = now.plus(validity);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("service-accounts")
                .subject(String.valueOf(account.id()))
                .claim("role", account.role().name())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthenticationToken(value, expiresAt);
    }
}
