package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.exception.AccountNotFoundException;
import fr.nordlys.shoploc.accounts.domain.exception.CardBalanceNotEmptyException;
import fr.nordlys.shoploc.accounts.domain.exception.OperationNotAllowedException;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.CardStatus;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;

/**
 * US-T02: a user consults, modifies or deletes their personal data (RGPD).
 * The account id always comes from the token, so a user only reaches their own data.
 */
public class PersonalDataService {

    // The name columns cannot be empty, so a deleted account gets this name
    static final String DELETED_FIRST_NAME = "Compte";
    static final String DELETED_LAST_NAME = "supprimé";

    private final AccountPort accountPort;
    private final LoyaltyCardPort cardPort;

    public PersonalDataService(AccountPort accountPort, LoyaltyCardPort cardPort) {
        this.accountPort = accountPort;
        this.cardPort = cardPort;
    }

    public Account getMyData(Long accountId) {
        Account account = accountPort.findById(accountId).orElse(null);
        if (account == null || account.status() == UserStatus.DELETED) {
            throw new AccountNotFoundException(accountId);
        }
        return account;
    }

    /** The e-mail cannot be changed for now: it would need a confirmation e-mail. */
    public Account updateMyData(Long accountId, String firstName, String lastName,
                                String phone, String licensePlate) {
        Account account = getMyData(accountId);

        if (isBlank(firstName) || isBlank(lastName)) {
            throw new IllegalArgumentException("First name and last name are required");
        }

        Account updated = new Account(account.id(), account.email(), firstName.trim(), lastName.trim(),
                blankToNull(phone), blankToNull(licensePlate), account.role(), account.status(),
                account.createdAt());
        return accountPort.save(updated);
    }

    /**
     * Deletes the account: the personal data is erased but the row is kept,
     * because the other services use its id. Refused if money is left on the card.
     */
    public void deleteMyAccount(Long accountId) {
        Account account = getMyData(accountId);
        if (account.role() != Role.CLIENT) {
            throw new OperationNotAllowedException("Only a client can delete their account here");
        }

        LoyaltyCard card = cardPort.findByAccountId(accountId).orElse(null);
        if (card != null && card.balance().signum() > 0) {
            throw new CardBalanceNotEmptyException(card.balance());
        }

        Account anonymized = new Account(account.id(), "deleted-" + account.id() + "@shoploc.invalid",
                DELETED_FIRST_NAME, DELETED_LAST_NAME, null, null, account.role(), UserStatus.DELETED,
                account.createdAt());
        accountPort.save(anonymized);
        accountPort.savePasswordHash(accountId, null);

        if (card != null) {
            LoyaltyCard blockedCard = new LoyaltyCard(card.id(), card.accountId(), card.qrCode(),
                    card.balance(), card.issuedInShop(), CardStatus.BLOCKED, card.issuedAt());
            cardPort.save(blockedCard);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
