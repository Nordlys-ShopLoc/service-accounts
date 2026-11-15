package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.OnlineRegistrationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import fr.nordlys.shoploc.accounts.infrastructure.security.BCryptPasswordHasher;
import fr.nordlys.shoploc.accounts.domain.port.PasswordHasher;
import fr.nordlys.shoploc.accounts.domain.exception.EmailAlreadyUsedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    private AccountPort accountPort;
    private OnlineRegistrationPort registrationPort;
    private LoyaltyCardService cardService;
    private RegistrationService service;
    private PasswordHasher encoder;

    @BeforeEach
    void setUp() {
        accountPort = mock(AccountPort.class);
        registrationPort = mock(OnlineRegistrationPort.class);
        cardService = mock(LoyaltyCardService.class);
        encoder = new BCryptPasswordHasher();
        service = new RegistrationService(accountPort, registrationPort, cardService, encoder);
    }

    // refuse les champs obligatoires manquants
    @Test
    void rejectsMissingFields() {
        assertThrows(IllegalArgumentException.class, () -> service.register("", "Martin", "client@test.fr", "Password123"));
        assertThrows(IllegalArgumentException.class, () -> service.register("Marie", "", "client@test.fr", "Password123"));
        assertThrows(IllegalArgumentException.class, () -> service.register("Marie", "Martin", "", "Password123"));
        verifyNoInteractions(registrationPort, cardService);
    }

    // refuse un email invalide
    @Test
    void rejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> service.register("Marie", "Martin", "incorrect", "Password123"));
        verifyNoInteractions(registrationPort, cardService);
    }

    // refuse un mot de passe trop court
    @Test
    void rejectsShortPassword() {
        assertThrows(IllegalArgumentException.class, () -> service.register("Marie", "Martin", "client@test.fr", "123"));
        verifyNoInteractions(registrationPort, cardService);
    }

    // refuse les emails deja utilises
    @Test
    void rejectsDuplicateEmail() {
        when(accountPort.existsByEmail("client@test.fr")).thenReturn(true);
        assertThrows(EmailAlreadyUsedException.class, () -> service.register("Marie", "Martin", "client@test.fr", "Password123"));
        verifyNoInteractions(registrationPort, cardService);
    }

    // enregistre le hash et attribue automatiquement une carte
    @Test
    void createsClientWithPasswordAndCard() {
        when(registrationPort.saveWithPassword(any(Account.class), anyString())).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            String hash = invocation.getArgument(1);
            assertTrue(encoder.matches("Password123", hash));
            assertNotEquals("Password123", hash);
            assertEquals(Role.CLIENT, account.role());
            return new Account(10L, account.email(), account.firstName(), account.lastName(),
                    account.phone(), account.licensePlate(), account.role(), account.status(), account.createdAt());
        });

        Account account = service.register("Marie", "Martin", " CLIENT@TEST.FR ", "Password123");

        assertEquals(10L, account.id());
        assertEquals("client@test.fr", account.email());
        verify(cardService).issueCard(10L, false);
    }
}
