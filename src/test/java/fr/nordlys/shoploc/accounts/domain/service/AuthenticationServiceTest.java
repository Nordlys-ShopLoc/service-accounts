package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.exception.AccountNotActiveException;
import fr.nordlys.shoploc.accounts.domain.exception.InvalidCredentialsException;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.AuthenticationToken;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.PasswordHasher;
import fr.nordlys.shoploc.accounts.domain.port.TokenIssuer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    private static final String EMAIL = "julie@mail.fr";
    private static final String HASH = "hashed-secret";

    private final AccountPort accountPort = mock(AccountPort.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final TokenIssuer tokenIssuer = mock(TokenIssuer.class);
    private final AuthenticationService service =
            new AuthenticationService(accountPort, passwordHasher, tokenIssuer);

    private final AuthenticationToken token = new AuthenticationToken("jwt", Instant.now().plusSeconds(3600));

    private Account givenAccount(UserStatus status) {
        Account account = new Account(1L, EMAIL, "Julie", "Martin", null, null,
                Role.CLIENT, status, LocalDateTime.now());
        when(accountPort.findByEmail(EMAIL)).thenReturn(Optional.of(account));
        when(accountPort.findPasswordHash(1L)).thenReturn(Optional.of(HASH));
        when(passwordHasher.matches("secret", HASH)).thenReturn(true);
        when(tokenIssuer.issue(account)).thenReturn(token);
        return account;
    }

    @Test
    void returns_a_token_when_email_and_password_are_correct() {
        givenAccount(UserStatus.ACTIVE);

        assertThat(service.login(EMAIL, "secret")).isEqualTo(token);
    }

    @Test
    void ignores_the_case_and_the_spaces_of_the_email() {
        givenAccount(UserStatus.ACTIVE);

        assertThat(service.login(" Julie@Mail.FR ", "secret")).isEqualTo(token);
    }

    @Test
    void refuses_a_wrong_password() {
        givenAccount(UserStatus.ACTIVE);

        assertThatThrownBy(() -> service.login(EMAIL, "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(tokenIssuer, never()).issue(any());
    }

    @Test
    void refuses_an_unknown_email_with_the_same_error() {
        when(accountPort.findByEmail("nobody@mail.fr")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("nobody@mail.fr", "secret"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refuses_an_account_created_in_shop_without_password() {
        givenAccount(UserStatus.ACTIVE);
        when(accountPort.findPasswordHash(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(EMAIL, "secret"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refuses_a_suspended_account() {
        givenAccount(UserStatus.SUSPENDED);

        assertThatThrownBy(() -> service.login(EMAIL, "secret"))
                .isInstanceOf(AccountNotActiveException.class);
        verify(tokenIssuer, never()).issue(any());
    }

    @Test
    void refuses_a_deleted_account() {
        givenAccount(UserStatus.DELETED);

        assertThatThrownBy(() -> service.login(EMAIL, "secret"))
                .isInstanceOf(AccountNotActiveException.class);
    }

    @Test
    void does_not_reveal_a_suspended_account_without_the_right_password() {
        givenAccount(UserStatus.SUSPENDED);

        assertThatThrownBy(() -> service.login(EMAIL, "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refuses_an_empty_email_or_password() {
        assertThatThrownBy(() -> service.login(" ", "secret"))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> service.login(EMAIL, null))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
