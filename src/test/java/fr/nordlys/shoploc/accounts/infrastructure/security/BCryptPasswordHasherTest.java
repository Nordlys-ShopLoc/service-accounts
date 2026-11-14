package fr.nordlys.shoploc.accounts.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void never_keeps_the_password_in_clear() {
        assertThat(hasher.hash("secret")).isNotEqualTo("secret").startsWith("$2");
    }

    @Test
    void recognises_only_the_right_password() {
        String hash = hasher.hash("secret");

        assertThat(hasher.matches("secret", hash)).isTrue();
        assertThat(hasher.matches("wrong", hash)).isFalse();
    }
}
