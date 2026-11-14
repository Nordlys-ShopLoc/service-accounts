
package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;
// permet de recuperer la carte de fidelite d'un client et de verifier qu'elle est active
public class LoyaltyCardQueryService {

    private final LoyaltyCardPort cardPort;

    public LoyaltyCardQueryService(LoyaltyCardPort cardPort) {
        this.cardPort = cardPort;
    }

    // recupere la carte de fidelite d'un client a partir de son id
    public LoyaltyCard getCardByAccountId(Long accountId) {

        LoyaltyCard card = cardPort.findByAccountId(accountId).orElseThrow(() -> new IllegalArgumentException("Carte de fidélité introuvable"));

        // Verifie si la carte est active
        if (!card.isUsable()) {
            throw new IllegalStateException("La carte de fidélité est bloquée");
        }

        return card;
    }

    // recupere une carte a partir de son qr code et verifie qu'elle est active
    public LoyaltyCard getCardByQrCode(String qrCode) {

        LoyaltyCard card = cardPort.findByQrCode(qrCode).orElseThrow(() -> new IllegalArgumentException("Carte de fidelite introuvable"));

        if (!card.isUsable()) {
            throw new IllegalStateException("La carte de fidelite est bloquee");
        }

        return card;
    }

}
