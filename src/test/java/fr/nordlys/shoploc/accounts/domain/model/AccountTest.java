package fr.nordlys.shoploc.accounts.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    private Account account(Role role, UserStatus status) {
        return new Account(1L, "julie@example.com", "Julie", "Martin",
                "0600000000", null, role, status, LocalDateTime.now());
    }

    @Test
    void builds_the_full_name_from_first_and_last_name() {
        assertThat(account(Role.CLIENT, UserStatus.ACTIVE).fullName())
                .isEqualTo("Julie Martin");
    }

    @Test
    void only_an_active_account_is_active() {
        assertThat(account(Role.CLIENT, UserStatus.ACTIVE).isActive()).isTrue();
        assertThat(account(Role.CLIENT, UserStatus.SUSPENDED).isActive()).isFalse();
    }

    @Test
    void only_the_admin_role_grants_administration() {
        assertThat(account(Role.ADMIN, UserStatus.ACTIVE).isAdmin()).isTrue();
        assertThat(account(Role.CLIENT, UserStatus.ACTIVE).isAdmin()).isFalse();
    }
}
