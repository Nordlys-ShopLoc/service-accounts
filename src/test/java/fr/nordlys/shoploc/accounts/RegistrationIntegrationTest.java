package fr.nordlys.shoploc.accounts;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;
import fr.nordlys.shoploc.accounts.domain.service.RegistrationService;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.entity.AccountEntity;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.repository.LoyaltyCardJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RegistrationIntegrationTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AccountJpaRepository accountRepository;

    @Autowired
    private LoyaltyCardJpaRepository cardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoSpyBean
    private LoyaltyCardPort cardPort;

    // verifie la creation du compte et de la carte en base
    @Test
    void shouldSaveAccountAndCard() {
        String email = "client-" + UUID.randomUUID() + "@test.fr";

        Account account = registrationService.register("Marie", "Martin", email, "Password123");

        AccountEntity saved = accountRepository.findByEmail(email).orElseThrow();

        assertEquals(Role.CLIENT, saved.getRole());
        assertNotEquals("Password123", saved.getPasswordHash());
        assertTrue(passwordEncoder.matches("Password123", saved.getPasswordHash()));

        var card = cardRepository.findByAccountId(account.id()).orElseThrow();

        assertNotNull(card.getId());
        assertNotNull(card.getQrCode());
        assertFalse(card.isIssuedInShop());
    }

    // verifie le rollback si la creation de la carte echoue
    @Test
    void shouldRollbackWhenCardCreationFails() {
        String email = "rollback-" + UUID.randomUUID() + "@test.fr";

        doThrow(new IllegalStateException("Erreur creation carte"))
                .when(cardPort).save(any());

        assertThrows(InvalidDataAccessApiUsageException.class,
                () -> registrationService.register("Marie", "Martin", email, "Password123"));

        assertFalse(accountRepository.findByEmail(email).isPresent());
    }
}
