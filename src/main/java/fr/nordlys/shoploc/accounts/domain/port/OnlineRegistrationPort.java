package fr.nordlys.shoploc.accounts.domain.port;

import fr.nordlys.shoploc.accounts.domain.model.Account;

// enregistre un compte avec son mot de passe deja encode
public interface OnlineRegistrationPort {
    Account saveWithPassword(Account account, String passwordHash);
}
