package fr.nordlys.shoploc.accounts.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Business rule RM01: the card balance is capped at 100 euros. */
class LoyaltyCardTest {

    private LoyaltyCard cardWithBalance(String amount) {
        return new LoyaltyCard(1L, 1L, "QR-TEST", new BigDecimal(amount),
                false, CardStatus.ACTIVE, LocalDateTime.now());
    }

    @Test
    void rejects_a_balance_above_100_euros() {
        assertThatThrownBy(() -> cardWithBalance("100.01"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void accepts_a_balance_of_exactly_100_euros() {
        assertThat(cardWithBalance("100.00").balance()).isEqualByComparingTo("100");
    }

    @Test
    void allows_a_top_up_only_within_the_cap() {
        LoyaltyCard card = cardWithBalance("80.00");
        assertThat(card.canTopUp(new BigDecimal("20.00"))).isTrue();
        assertThat(card.canTopUp(new BigDecimal("20.01"))).isFalse();
    }
}
