package fr.nordlys.shoploc.accounts.domain.exception;

/** An e-mail address can only be used by one account. */
public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("E-mail already used: " + email);
    }
}
