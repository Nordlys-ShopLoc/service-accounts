package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.exception.AccountNotActiveException;
import fr.nordlys.shoploc.accounts.domain.exception.AccountNotFoundException;
import fr.nordlys.shoploc.accounts.domain.exception.OperationNotAllowedException;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.PasswordHasher;
import fr.nordlys.shoploc.accounts.domain.port.TokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountManagementServiceTest {

    @Mock
    private AccountPort accountPort;

    @InjectMocks
    private AccountManagementService service;

    private Account activeAdmin;
    private Account activeClient;
    private Account suspendedClient;
    private Account deletedClient;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        activeAdmin = new Account(1L, "admin@shoploc.fr", "Admin", "ShopLoc", null, null, Role.ADMIN, UserStatus.ACTIVE, now);
        activeClient = new Account(2L, "pierre@mail.fr", "Pierre", "Dupont", "0600000000", null, Role.CLIENT, UserStatus.ACTIVE, now);
        suspendedClient = new Account(3L, "julie@mail.fr", "Julie", "Martin", null, null, Role.CLIENT, UserStatus.SUSPENDED, now);
        deletedClient = new Account(4L, null, "Anonyme", "Anonyme", null, null, Role.CLIENT, UserStatus.DELETED, now);
    }

    @Test
    void shouldRejectWhenCallerIsNotAdmin() {
        when(accountPort.findById(2L)).thenReturn(Optional.of(activeClient));
        assertThrows(OperationNotAllowedException.class, () -> service.listAccounts(2L, null));
    }

    @Test
    void shouldListAllAccountsWhenNoRoleFilter() {
        when(accountPort.findById(1L)).thenReturn(Optional.of(activeAdmin));
        when(accountPort.findAll()).thenReturn(List.of(activeAdmin, activeClient));

        List<Account> result = service.listAccounts(1L, null);

        assertEquals(2, result.size());
        verify(accountPort).findAll();
    }

    @Test
    void shouldListAccountsFilteredByRole() {
        when(accountPort.findById(1L)).thenReturn(Optional.of(activeAdmin));
        when(accountPort.findByRole(Role.CLIENT)).thenReturn(List.of(activeClient));

        List<Account> result = service.listAccounts(1L, Role.CLIENT);

        assertEquals(1, result.size());
        assertEquals(Role.CLIENT, result.get(0).role());
    }

    @Test
    void shouldSuspendActiveAccount() {
        when(accountPort.findById(1L)).thenReturn(Optional.of(activeAdmin));
        when(accountPort.findById(2L)).thenReturn(Optional.of(activeClient));
        when(accountPort.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account updated = service.suspendAccount(1L, 2L);

        assertEquals(UserStatus.SUSPENDED, updated.status());
    }

    @Test
    void shouldPreventAdminFromSuspendingOwnAccount() {
        when(accountPort.findById(1L)).thenReturn(Optional.of(activeAdmin));
        assertThrows(OperationNotAllowedException.class, () -> service.suspendAccount(1L, 1L));
    }

    @Test
    void shouldReactivateSuspendedAccount() {
        when(accountPort.findById(1L)).thenReturn(Optional.of(activeAdmin));
        when(accountPort.findById(3L)).thenReturn(Optional.of(suspendedClient));
        when(accountPort.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account updated = service.reactivateAccount(1L, 3L);

        assertEquals(UserStatus.ACTIVE, updated.status());
    }

    @Test
    void shouldChangeAccountRole() {
        when(accountPort.findById(1L)).thenReturn(Optional.of(activeAdmin));
        when(accountPort.findById(2L)).thenReturn(Optional.of(activeClient));
        when(accountPort.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account police = service.changeRole(1L, 2L, Role.POLICE);
        assertEquals(Role.POLICE, police.role());
    }

    @Test
    void shouldPreventModifyingDeletedAccount() {
        when(accountPort.findById(1L)).thenReturn(Optional.of(activeAdmin));
        when(accountPort.findById(4L)).thenReturn(Optional.of(deletedClient));

        assertThrows(OperationNotAllowedException.class, () -> service.suspendAccount(1L, 4L));
    }

    @Test
    void suspendedAccountShouldNotBeAbleToLogin() {
        PasswordHasher passwordHasher = mock(PasswordHasher.class);
        TokenIssuer tokenIssuer = mock(TokenIssuer.class);
        AuthenticationService authService = new AuthenticationService(accountPort, passwordHasher, tokenIssuer);

        when(accountPort.findById(1L)).thenReturn(Optional.of(activeAdmin));
        when(accountPort.findById(2L)).thenReturn(Optional.of(activeClient));
        when(accountPort.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account suspended = service.suspendAccount(1L, 2L);

        when(accountPort.findByEmail("pierre@mail.fr")).thenReturn(Optional.of(suspended));
        when(accountPort.findPasswordHash(2L)).thenReturn(Optional.of("hashed-pwd"));
        when(passwordHasher.matches("secret", "hashed-pwd")).thenReturn(true);

        assertThrows(AccountNotActiveException.class, () -> authService.login("pierre@mail.fr", "secret"));
        verifyNoInteractions(tokenIssuer);
    }
}