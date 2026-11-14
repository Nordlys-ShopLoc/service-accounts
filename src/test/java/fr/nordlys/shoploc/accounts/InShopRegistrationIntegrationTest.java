package fr.nordlys.shoploc.accounts;

import fr.nordlys.shoploc.accounts.api.controller.InShopRegistrationController;
import fr.nordlys.shoploc.accounts.api.dto.InShopRegistrationRequest;
import fr.nordlys.shoploc.accounts.api.dto.InShopRegistrationResponse;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.entity.LoyaltyCardEntity;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.repository.LoyaltyCardJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/** US-S16 with a real database: the account and the card are created together or not at all. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class InShopRegistrationIntegrationTest {

    @Autowired
    private InShopRegistrationController controller;

    @Autowired
    private AccountPort accountPort;

    @Autowired
    private AccountJpaRepository accountRepository;

    @Autowired
    private LoyaltyCardJpaRepository cardRepository;

    @MockitoSpyBean
    private LoyaltyCardPort cardPort;

    /** Saves a merchant in the database and returns them as the logged-in user. */
    private Authentication loggedInMerchant() {
        String email = "merchant-" + UUID.randomUUID() + "@test.fr";
        Account merchant = accountPort.save(new Account(null, email, "Julie", "Martin",
                null, null, Role.MERCHANT, UserStatus.ACTIVE, null));
        return new TestingAuthenticationToken(String.valueOf(merchant.id()), null, "ROLE_MERCHANT");
    }

    @Test
    void saves_the_client_and_the_card_in_the_database() {
        String email = "client-" + UUID.randomUUID() + "@test.fr";

        InShopRegistrationResponse response = controller.register(
                new InShopRegistrationRequest("Pierre", "Durand", email, null), loggedInMerchant());

        assertThat(accountRepository.findByEmail(email)).isPresent();
        LoyaltyCardEntity card = cardRepository.findByAccountId(response.accountId()).orElseThrow();
        assertThat(card.isIssuedInShop()).isTrue();
        assertThat(card.getQrCode()).isEqualTo(response.qrCode());
    }

    @Test
    void saves_nothing_when_the_card_cannot_be_created() {
        String email = "rollback-" + UUID.randomUUID() + "@test.fr";
        Authentication merchant = loggedInMerchant();
        doThrow(new IllegalStateException("Card creation failed")).when(cardPort).save(any());

        assertThatThrownBy(() -> controller.register(
                new InShopRegistrationRequest("Pierre", "Durand", email, null), merchant))
                .isInstanceOf(RuntimeException.class);

        // The transaction was cancelled: the account was not kept
        assertThat(accountRepository.findByEmail(email)).isEmpty();
    }
}
