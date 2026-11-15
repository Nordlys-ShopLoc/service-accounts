package fr.nordlys.shoploc.accounts.domain.exception;

public class CardBlockedException extends RuntimeException {

    public CardBlockedException() {
        super("La carte de fidélité est bloquée");
    }
}
