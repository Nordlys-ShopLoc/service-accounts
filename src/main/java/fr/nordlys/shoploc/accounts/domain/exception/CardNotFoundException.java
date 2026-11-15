package fr.nordlys.shoploc.accounts.domain.exception;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException() {
        super("Carte de fidélité introuvable");
    }
}
