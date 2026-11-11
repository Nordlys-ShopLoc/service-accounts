package fr.nordlys.shoploc.accounts.infrastructure.web.controller;

import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardQueryService;
import fr.nordlys.shoploc.accounts.infrastructure.web.dto.LoyaltyCardResponse;

// prepare les operations de consultation des cartes de fidelite
// les routes seront ajoutees apres l'integration de l'authentification
public class LoyaltyCardController {

    private final LoyaltyCardQueryService service;

    public LoyaltyCardController(LoyaltyCardQueryService service) {
        this.service = service;
    }

    // recupere les informations d'une carte a partir du compte
    public LoyaltyCardResponse getCardByAccountId(Long accountId) {
        return LoyaltyCardResponse.from(service.getCardByAccountId(accountId));
    }

    // recupere les informations d'une carte a partir du qr code
    public LoyaltyCardResponse getCardByQrCode(String qrCode) {
        return LoyaltyCardResponse.from(service.getCardByQrCode(qrCode));
    }
}
