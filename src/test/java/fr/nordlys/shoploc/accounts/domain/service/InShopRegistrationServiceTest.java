package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.exception.EmailAlreadyUsedException;
import fr.nordlys.shoploc.accounts.domain.exception.OperationNotAllowedException;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** US-S16: a merchant registers a client in the shop. */
class InShopRegistrationServiceTest {

    private static final Long MERCHANT_ID = 1L;
    private static final Long NEW_CLIENT_ID = 2L;

    private final AccountPort accountPort = mock(AccountPort.class);
    private final LoyaltyCardService loyaltyCardService = mock(LoyaltyCardService.class);
    private final InShopRegistrationService service =
            new InShopRegistrationService(accountPort, loyaltyCardService);

    @BeforeEach
    void setUp() {
        when(accountPort.findById(MERCHANT_ID))
                .thenReturn(Optional.of(account(MERCHANT_ID, Role.MERCHANT, UserStatus.ACTIVE)));

        // The fake database gives the id 2 to the new account
        when(accountPort.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            return new Account(NEW_CLIENT_ID, account.email(), account.firstName(), account.lastName(),
                    account.phone(), account.licensePlate(), account.role(), account.status(),
                    LocalDateTime.now());
        });
    }

    private static Account account(Long id, Role role, UserStatus status) {
        return new Account(id, "user" + id + "@mail.fr", "Julie", "Martin",
                null, null, role, status, LocalDateTime.now());
    }

    /** Gets the account that the service gave to accountPort.save(). */
    private Account savedAccount() {
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountPort).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void creates_a_client_account_and_a_card_given_in_shop() {
        service.registerClient(MERCHANT_ID, "Pierre", "Durand", "pierre@mail.fr", "0611223344");

        Account client = savedAccount();
        assertThat(client.role()).isEqualTo(Role.CLIENT);
        assertThat(client.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(client.firstName()).isEqualTo("Pierre");
        assertThat(client.lastName()).isEqualTo("Durand");
        assertThat(client.email()).isEqualTo("pierre@mail.fr");
        assertThat(client.phone()).isEqualTo("0611223344");
        verify(loyaltyCardService).issueCard(NEW_CLIENT_ID, true);
    }

    @Test
    void accepts_a_client_without_email_or_phone() {
        service.registerClient(MERCHANT_ID, "Pierre", "Durand", null, "  ");

        Account client = savedAccount();
        assertThat(client.email()).isNull();
        assertThat(client.phone()).isNull();
        verify(accountPort, never()).existsByEmail(any());
        verify(loyaltyCardService).issueCard(NEW_CLIENT_ID, true);
    }

    @Test
    void saves_the_email_in_lower_case_without_spaces() {
        service.registerClient(MERCHANT_ID, "Pierre", "Durand", " Pierre@Mail.FR ", null);

        assertThat(savedAccount().email()).isEqualTo("pierre@mail.fr");
    }

    @Test
    void refuses_an_email_already_used() {
        when(accountPort.existsByEmail("pierre@mail.fr")).thenReturn(true);

        assertThatThrownBy(() -> service.registerClient(MERCHANT_ID, "Pierre", "Durand", "Pierre@mail.fr", null))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(accountPort, never()).save(any());
        verify(loyaltyCardService, never()).issueCard(any(), anyBoolean());
    }

    @Test
    void requires_a_first_name_and_a_last_name() {
        assertThatThrownBy(() -> service.registerClient(MERCHANT_ID, " ", "Durand", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.registerClient(MERCHANT_ID, "Pierre", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(accountPort, never()).save(any());
    }

    @Test
    void refuses_a_user_who_is_not_a_merchant() {
        when(accountPort.findById(5L)).thenReturn(Optional.of(account(5L, Role.CLIENT, UserStatus.ACTIVE)));

        assertThatThrownBy(() -> service.registerClient(5L, "Pierre", "Durand", null, null))
                .isInstanceOf(OperationNotAllowedException.class);
        verify(accountPort, never()).save(any());
    }

    @Test
    void refuses_a_suspended_merchant() {
        when(accountPort.findById(6L)).thenReturn(Optional.of(account(6L, Role.MERCHANT, UserStatus.SUSPENDED)));

        assertThatThrownBy(() -> service.registerClient(6L, "Pierre", "Durand", null, null))
                .isInstanceOf(OperationNotAllowedException.class);
        verify(accountPort, never()).save(any());
    }

    @Test
    void refuses_an_unknown_merchant() {
        // Nothing is configured for id 99: the fake database returns an empty Optional
        assertThatThrownBy(() -> service.registerClient(99L, "Pierre", "Durand", null, null))
                .isInstanceOf(OperationNotAllowedException.class);
        verify(accountPort, never()).save(any());
    }
}
