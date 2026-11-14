package fr.nordlys.shoploc.accounts.domain.port;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.AuthenticationToken;

public interface TokenIssuer {

    AuthenticationToken issue(Account account);
}
