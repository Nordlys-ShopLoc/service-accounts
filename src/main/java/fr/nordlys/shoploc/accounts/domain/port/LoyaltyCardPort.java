package fr.nordlys.shoploc.accounts.domain.port;

import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;

import java.util.Optional;

/** Persistence port for loyalty cards. */
public interface LoyaltyCardPort {

    Optional<LoyaltyCard> findByAccountId(Long accountId);

    /** Lookup by QR code: used when a merchant scans a card. */
    Optional<LoyaltyCard> findByQrCode(String qrCode);

    LoyaltyCard save(LoyaltyCard card);
}
