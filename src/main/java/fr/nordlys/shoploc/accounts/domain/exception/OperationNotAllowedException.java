package fr.nordlys.shoploc.accounts.domain.exception;

/** The user is not allowed to do this operation. */
public class OperationNotAllowedException extends RuntimeException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
