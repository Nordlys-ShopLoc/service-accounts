package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.exception.AccountNotFoundException;
import fr.nordlys.shoploc.accounts.domain.exception.CardBalanceNotEmptyException;
import fr.nordlys.shoploc.accounts.domain.exception.OperationNotAllowedException;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.CardStatus;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** US-T02: a user consults, modifies or deletes their personal data. */
class PersonalDataServiceTest {

    private static final Long CLIENT_ID = 7L;

    private final AccountPort accountPort = mock(AccountPort.class);
    private final LoyaltyCardPort cardPort = mock(LoyaltyCardPort.class);
    private final PersonalDataService service = new PersonalDataService(accountPort, cardPort);

    private final Account pierre = new Account(CLIENT_ID, "pierre@mail.fr", "Pierre", "Durand",
            "0611223344", "AB-123-CD", Role.CLIENT, UserStatus.ACTIVE, LocalDateTime.now());

    @BeforeEach
    void setUp() {
        when(accountPort.findById(CLIENT_ID)).thenReturn(Optional.of(pierre));
        // The fake database gives back the account it receives
        when(accountPort.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static LoyaltyCard cardWithBalance(String balance) {
        return new LoyaltyCard(3L, CLIENT_ID, "SL-ABC", new BigDecimal(balance),
                false, CardStatus.ACTIVE, LocalDateTime.now());
    }

    /** Gets the account that the service gave to accountPort.save(). */
    private Account savedAccount() {
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountPort).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void returns_the_data_of_the_logged_in_user() {
        assertThat(service.getMyData(CLIENT_ID)).isEqualTo(pierre);
    }

    @Test
    void does_not_find_an_unknown_or_deleted_account() {
        Account deleted = new Account(8L, "deleted-8@shoploc.invalid", "Compte", "supprimé",
                null, null, Role.CLIENT, UserStatus.DELETED, LocalDateTime.now());
        when(accountPort.findById(8L)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> service.getMyData(99L)).isInstanceOf(AccountNotFoundException.class);
        assertThatThrownBy(() -> service.getMyData(8L)).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void updates_names_phone_and_plate_but_keeps_the_email() {
        service.updateMyData(CLIENT_ID, " Pierre-Louis ", "Durand", "0699887766", "XY-987-ZT");

        Account saved = savedAccount();
        assertThat(saved.id()).isEqualTo(CLIENT_ID);
        assertThat(saved.firstName()).isEqualTo("Pierre-Louis");
        assertThat(saved.phone()).isEqualTo("0699887766");
        assertThat(saved.licensePlate()).isEqualTo("XY-987-ZT");
        assertThat(saved.email()).isEqualTo("pierre@mail.fr");
    }

    @Test
    void erases_the_phone_and_the_plate_when_left_empty() {
        service.updateMyData(CLIENT_ID, "Pierre", "Durand", "", null);

        Account saved = savedAccount();
        assertThat(saved.phone()).isNull();
        assertThat(saved.licensePlate()).isNull();
    }

    @Test
    void requires_a_first_name_and_a_last_name() {
        assertThatThrownBy(() -> service.updateMyData(CLIENT_ID, "Pierre", " ", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(accountPort, never()).save(any());
    }

    @Test
    void refuses_the_deletion_while_money_is_left_on_the_card() {
        when(cardPort.findByAccountId(CLIENT_ID)).thenReturn(Optional.of(cardWithBalance("12.50")));

        assertThatThrownBy(() -> service.deleteMyAccount(CLIENT_ID))
                .isInstanceOf(CardBalanceNotEmptyException.class);
        verify(accountPort, never()).save(any());
        verify(accountPort, never()).savePasswordHash(any(), any());
        verify(cardPort, never()).save(any());
    }

    @Test
    void erases_the_personal_data_but_keeps_the_account_id() {
        when(cardPort.findByAccountId(CLIENT_ID)).thenReturn(Optional.of(cardWithBalance("0")));

        service.deleteMyAccount(CLIENT_ID);

        Account saved = savedAccount();
        assertThat(saved.id()).isEqualTo(CLIENT_ID);
        assertThat(saved.email()).isEqualTo("deleted-7@shoploc.invalid");
        assertThat(saved.firstName()).isEqualTo(PersonalDataService.DELETED_FIRST_NAME);
        assertThat(saved.lastName()).isEqualTo(PersonalDataService.DELETED_LAST_NAME);
        assertThat(saved.phone()).isNull();
        assertThat(saved.licensePlate()).isNull();
        assertThat(saved.status()).isEqualTo(UserStatus.DELETED);
        verify(accountPort).savePasswordHash(CLIENT_ID, null);
    }

    @Test
    void blocks_the_card_when_the_account_is_deleted() {
        when(cardPort.findByAccountId(CLIENT_ID)).thenReturn(Optional.of(cardWithBalance("0")));

        service.deleteMyAccount(CLIENT_ID);

        ArgumentCaptor<LoyaltyCard> captor = ArgumentCaptor.forClass(LoyaltyCard.class);
        verify(cardPort).save(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(3L);
        assertThat(captor.getValue().status()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    void only_a_client_can_delete_their_account() {
        Account merchant = new Account(9L, "julie@shop.fr", "Julie", "Martin",
                null, null, Role.MERCHANT, UserStatus.ACTIVE, LocalDateTime.now());
        when(accountPort.findById(9L)).thenReturn(Optional.of(merchant));

        assertThatThrownBy(() -> service.deleteMyAccount(9L))
                .isInstanceOf(OperationNotAllowedException.class);
        verify(accountPort, never()).save(any());
    }
}
