package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.exception.CardBlockedException;
import fr.nordlys.shoploc.accounts.domain.exception.CardNotFoundException;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;

public class LoyaltyCardQueryService {

    private final LoyaltyCardPort cardPort;

    public LoyaltyCardQueryService(LoyaltyCardPort cardPort) {
        this.cardPort = cardPort;
    }

    public LoyaltyCard getCardByAccountId(Long accountId) {
        LoyaltyCard card = cardPort.findByAccountId(accountId)
                .orElseThrow(CardNotFoundException::new);

        if (!card.isUsable()) {
            throw new CardBlockedException();
        }

        return card;
    }

    public LoyaltyCard getCardByQrCode(String qrCode) {
        LoyaltyCard card = cardPort.findByQrCode(qrCode)
                .orElseThrow(CardNotFoundException::new);

        if (!card.isUsable()) {
            throw new CardBlockedException();
        }

        return card;
    }
}
