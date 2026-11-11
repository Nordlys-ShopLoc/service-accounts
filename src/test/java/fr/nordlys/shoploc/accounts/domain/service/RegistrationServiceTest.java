package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    private AccountPort accountPort;
    private LoyaltyCardService cardService;
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        accountPort = mock(AccountPort.class);
        cardService = mock(LoyaltyCardService.class);
        registrationService = new RegistrationService(accountPort, cardService);
    }

    // verifie les champs obligatoires
    @Test
    void shouldRejectMissingFields() {
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.register("", "Martin", "client@test.fr"));

        assertThrows(IllegalArgumentException.class,
                () -> registrationService.register("Marie", "", "client@test.fr"));

        assertThrows(IllegalArgumentException.class,
                () -> registrationService.register("Marie", "Martin", ""));

        verifyNoInteractions(accountPort, cardService);
    }

    // verifie le format de l'adresse email
    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.register("Marie", "Martin", "email-invalide"));

        verifyNoInteractions(accountPort, cardService);
    }

    // empeche la creation de deux comptes avec le meme email
    @Test
    void shouldRejectDuplicateEmail() {
        when(accountPort.existsByEmail("client@test.fr")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> registrationService.register("Marie", "Martin", "client@test.fr"));

        verify(accountPort, never()).save(any());
        verifyNoInteractions(cardService);
    }

    // cree un client et lui attribue une carte numerique
    @Test
    void shouldRegisterClientAndIssueCard() {
        when(accountPort.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);

            return new Account(10L, account.email(), account.firstName(),
                    account.lastName(), account.phone(), account.licensePlate(),
                    account.role(), account.status(), account.createdAt());
        });

        Account account = registrationService.register("Marie", "Martin", " CLIENT@TEST.FR ");

        assertEquals(10L, account.id());
        assertEquals("client@test.fr", account.email());
        assertEquals(Role.CLIENT, account.role());

        verify(accountPort).save(any(Account.class));
        verify(cardService).issueCard(10L, false);
    }
}
