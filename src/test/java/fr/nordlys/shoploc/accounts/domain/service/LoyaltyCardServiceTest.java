package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.exception.CardAlreadyIssuedException;
import fr.nordlys.shoploc.accounts.domain.model.CardStatus;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class LoyaltyCardServiceTest {

    private final LoyaltyCardPort cardPort = mock(LoyaltyCardPort.class);
    private final LoyaltyCardService service = new LoyaltyCardService(cardPort);

    @BeforeEach
    void setUp() {
        // The fake database gives back the card it receives
        when(cardPort.save(any(LoyaltyCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void creates_an_active_card_with_a_balance_at_zero() {
        LoyaltyCard card = service.issueCard(7L, false);

        assertThat(card.accountId()).isEqualTo(7L);
        assertThat(card.balance()).isEqualByComparingTo("0");
        assertThat(card.status()).isEqualTo(CardStatus.ACTIVE);
        assertThat(card.issuedInShop()).isFalse();
        assertThat(card.qrCode()).startsWith("SL-");
    }

    @Test
    void remembers_that_the_card_was_given_in_shop() {
        LoyaltyCard card = service.issueCard(7L, true);

        assertThat(card.issuedInShop()).isTrue();
    }

    @Test
    void gives_a_different_qr_code_to_each_card() {
        LoyaltyCard first = service.issueCard(1L, false);
        LoyaltyCard second = service.issueCard(2L, false);

        assertThat(first.qrCode()).isNotEqualTo(second.qrCode());
    }

    @Test
    void refuses_a_second_card_for_the_same_account() {
        LoyaltyCard existingCard = new LoyaltyCard(1L, 7L, "SL-OLD", BigDecimal.ZERO,
                false, CardStatus.ACTIVE, LocalDateTime.now());
        when(cardPort.findByAccountId(7L)).thenReturn(Optional.of(existingCard));

        assertThatThrownBy(() -> service.issueCard(7L, false))
                .isInstanceOf(CardAlreadyIssuedException.class);
        verify(cardPort, never()).save(any());
    }
}
