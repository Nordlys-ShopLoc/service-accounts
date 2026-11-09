package fr.nordlys.shoploc.accounts.domain.model;

import java.time.LocalDateTime;

/**
 * A client's loyalty card.
 *
 * The QR code identifies the card, not the account: the merchant scans it
 * during an in-shop purchase. The balance is the top-up wallet, capped at
 * 100 euros (RM01). Loyalty points belong to service-loyalty.
 */
public record LoyaltyCard(
        Long id,
        Long accountId,
        String qrCode,
        int balanceCents,
        boolean issuedInShop,
        CardStatus status,
        LocalDateTime issuedAt
) {

    /** Maximum card balance, in cents (RM01). */
    public static final int MAX_BALANCE_CENTS = 10_000;

    public LoyaltyCard {
        if (balanceCents < 0 || balanceCents > MAX_BALANCE_CENTS) {
            throw new IllegalArgumentException(
                    "Balance must be between 0 and " + MAX_BALANCE_CENTS + " cents");
        }
    }

    /** Balance in euros, for display purposes. */
    public double balanceEuros() {
        return balanceCents / 100.0;
    }

    public boolean isUsable() {
        return status == CardStatus.ACTIVE;
    }

    /** Checks that a top-up would not exceed the cap (RM01). */
    public boolean canTopUp(int amountCents) {
        return amountCents > 0 && balanceCents + amountCents <= MAX_BALANCE_CENTS;
    }
}
