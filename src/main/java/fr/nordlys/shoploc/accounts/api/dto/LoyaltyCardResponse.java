package fr.nordlys.shoploc.accounts.api.dto;

import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.model.CardStatus;

// contient les informations de la carte envoyees au frontend
public record LoyaltyCardResponse(Long id, String qrCode, CardStatus status) {

    // transforme une carte en reponse pour le frontend
    public static LoyaltyCardResponse from(LoyaltyCard card) {
        return new LoyaltyCardResponse(card.id(), card.qrCode(), card.status());
    }
}
