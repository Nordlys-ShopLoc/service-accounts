package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.exception.AccountNotActiveException;
import fr.nordlys.shoploc.accounts.domain.exception.InvalidCredentialsException;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.AuthenticationToken;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.PasswordHasher;
import fr.nordlys.shoploc.accounts.domain.port.TokenIssuer;

public class AuthenticationService {

    private final AccountPort accountPort;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public AuthenticationService(AccountPort accountPort, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.accountPort = accountPort;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    public AuthenticationToken login(String email, String password) {
        if (isBlank(email) || isBlank(password)) {
            throw new InvalidCredentialsException();
        }
        Account account = accountPort.findByEmail(email.trim().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);
        String passwordHash = accountPort.findPasswordHash(account.id())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordHasher.matches(password, passwordHash)) {
            throw new InvalidCredentialsException();
        }
        // Checked after the password: only the owner of the account learns that it is suspended
        if (!account.isActive()) {
            throw new AccountNotActiveException();
        }
        return tokenIssuer.issue(account);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
