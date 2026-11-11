package fr.nordlys.shoploc.accounts.domain.exception;

public class CardAlreadyIssuedException extends RuntimeException {

    public CardAlreadyIssuedException(Long accountId) {
        super("Account " + accountId + " already has a loyalty card");
    }
}
