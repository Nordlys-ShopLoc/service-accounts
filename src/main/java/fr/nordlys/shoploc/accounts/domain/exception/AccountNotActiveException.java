package fr.nordlys.shoploc.accounts.domain.exception;

public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException() {
        super("This account is not active");
    }
}
