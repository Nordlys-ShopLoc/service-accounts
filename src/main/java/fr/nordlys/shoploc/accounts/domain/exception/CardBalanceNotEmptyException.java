package fr.nordlys.shoploc.accounts.domain.exception;

import java.math.BigDecimal;

/** An account cannot be deleted while money is left on its card: it is the client's money. */
public class CardBalanceNotEmptyException extends RuntimeException {

    public CardBalanceNotEmptyException(BigDecimal balance) {
        super("There are still " + balance + " euros on the card");
    }
}
