package fr.nordlys.shoploc.accounts;

import fr.nordlys.shoploc.accounts.api.controller.PersonalDataController;
import fr.nordlys.shoploc.accounts.domain.exception.CardBalanceNotEmptyException;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.CardStatus;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;
import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardService;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.entity.AccountEntity;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.repository.LoyaltyCardJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-T02 with a real database: what is really erased when a client deletes their account. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PersonalDataIntegrationTest {

    @Autowired
    private PersonalDataController controller;

    @Autowired
    private AccountPort accountPort;

    @Autowired
    private LoyaltyCardPort cardPort;

    @Autowired
    private LoyaltyCardService loyaltyCardService;

    @Autowired
    private AccountJpaRepository accountRepository;

    @Autowired
    private LoyaltyCardJpaRepository cardRepository;

    /** Saves a client with a password and a loyalty card. */
    private Account clientWithCard() {
        String email = "client-" + UUID.randomUUID() + "@test.fr";
        Account client = accountPort.save(new Account(null, email, "Pierre", "Durand",
                "0611223344", "AB-123-CD", Role.CLIENT, UserStatus.ACTIVE, null));
        accountPort.savePasswordHash(client.id(), "some-hash");
        loyaltyCardService.issueCard(client.id(), false);
        return client;
    }

    private Authentication loggedIn(Account account) {
        return new TestingAuthenticationToken(String.valueOf(account.id()), null, "ROLE_CLIENT");
    }

    @Test
    void erases_the_personal_data_in_the_database() {
        Account client = clientWithCard();

        controller.deleteMyAccount(loggedIn(client));

        AccountEntity saved = accountRepository.findById(client.id()).orElseThrow();
        assertThat(saved.getEmail()).isEqualTo("deleted-" + client.id() + "@shoploc.invalid");
        assertThat(saved.getFirstName()).isEqualTo("Compte");
        assertThat(saved.getPhone()).isNull();
        assertThat(saved.getLicensePlate()).isNull();
        assertThat(saved.getPasswordHash()).isNull();
        assertThat(saved.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(cardRepository.findByAccountId(client.id()).orElseThrow().getStatus())
                .isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    void keeps_everything_when_money_is_left_on_the_card() {
        Account client = clientWithCard();
        LoyaltyCard card = cardPort.findByAccountId(client.id()).orElseThrow();
        cardPort.save(new LoyaltyCard(card.id(), card.accountId(), card.qrCode(), new BigDecimal("12.50"),
                card.issuedInShop(), card.status(), card.issuedAt()));

        assertThatThrownBy(() -> controller.deleteMyAccount(loggedIn(client)))
                .isInstanceOf(CardBalanceNotEmptyException.class);

        AccountEntity saved = accountRepository.findById(client.id()).orElseThrow();
        assertThat(saved.getEmail()).isEqualTo(client.email());
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.getPasswordHash()).isEqualTo("some-hash");
    }
}
