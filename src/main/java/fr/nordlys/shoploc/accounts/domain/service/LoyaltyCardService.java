package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.exception.CardAlreadyIssuedException;
import fr.nordlys.shoploc.accounts.domain.model.CardStatus;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Creates loyalty cards.
 * Used by online sign-up (US-C01) and in-shop registration (US-S16).
 */
public class LoyaltyCardService {

    private final LoyaltyCardPort cardPort;

    public LoyaltyCardService(LoyaltyCardPort cardPort) {
        this.cardPort = cardPort;
    }

    /** Creates the card of an account: active, balance at 0, with a new QR code. */
    public LoyaltyCard issueCard(Long accountId, boolean issuedInShop) {
        if (cardPort.findByAccountId(accountId).isPresent()) {
            throw new CardAlreadyIssuedException(accountId);
        }
        LoyaltyCard card = new LoyaltyCard(null, accountId, generateQrCode(), BigDecimal.ZERO,
                issuedInShop, CardStatus.ACTIVE, null);
        return cardPort.save(card);
    }

    /** Random code that cannot be guessed: "SL-" followed by 32 characters. */
    private String generateQrCode() {
        return "SL-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
