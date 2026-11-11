package fr.nordlys.shoploc.accounts.domain.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid e-mail or password");
    }
}
