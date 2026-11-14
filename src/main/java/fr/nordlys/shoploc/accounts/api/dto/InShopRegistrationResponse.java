package fr.nordlys.shoploc.accounts.api.dto;

/** The new client and the QR code of the card to hand over. */
public record InShopRegistrationResponse(Long accountId, String qrCode) {
}
