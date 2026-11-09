package fr.nordlys.shoploc.accounts.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A client's loyalty card.
 *
 * The QR code identifies the card, not the account: the merchant scans it
 * during an in-shop purchase. The balance is the top-up wallet, in euros,
 * capped at 100 euros (RM01). Loyalty points belong to service-loyalty.
 */
public record LoyaltyCard(
        Long id,
        Long accountId,
        String qrCode,
        BigDecimal balance,
        boolean issuedInShop,
        CardStatus status,
        LocalDateTime issuedAt
) {

    /** Maximum card balance, in euros (RM01). */
    public static final BigDecimal MAX_BALANCE = new BigDecimal("100.00");

    public LoyaltyCard {
        if (balance == null || balance.signum() < 0 || balance.compareTo(MAX_BALANCE) > 0) {
            throw new IllegalArgumentException("Balance must be between 0 and 100 euros");
        }
    }

    public boolean isUsable() {
        return status == CardStatus.ACTIVE;
    }

    /** Checks that a top-up would not exceed the cap (RM01). */
    public boolean canTopUp(BigDecimal amount) {
        return amount.signum() > 0 && balance.add(amount).compareTo(MAX_BALANCE) <= 0;
    }
}
