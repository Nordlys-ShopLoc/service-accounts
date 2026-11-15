package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.domain.exception.AccountNotActiveException;
import fr.nordlys.shoploc.accounts.domain.exception.CardAlreadyIssuedException;
import fr.nordlys.shoploc.accounts.domain.exception.EmailAlreadyUsedException;
import fr.nordlys.shoploc.accounts.domain.exception.InvalidCredentialsException;
import fr.nordlys.shoploc.accounts.domain.exception.OperationNotAllowedException;
import fr.nordlys.shoploc.accounts.domain.exception.CardBlockedException;
import fr.nordlys.shoploc.accounts.domain.exception.CardNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ProblemDetail badRequest(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Informations invalides");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail unauthorized(InvalidCredentialsException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler({AccountNotActiveException.class, OperationNotAllowedException.class})
    ProblemDetail forbidden(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler({EmailAlreadyUsedException.class, CardAlreadyIssuedException.class})
    ProblemDetail conflict(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CardNotFoundException.class)
    ProblemDetail cardNotFound(CardNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(CardBlockedException.class)
    ProblemDetail cardBlocked(CardBlockedException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, exception.getMessage());
    }
}
