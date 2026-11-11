package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

// gere la creation des comptes clients
public class RegistrationService {

    private final AccountPort accountPort;
    private final LoyaltyCardService loyaltyCardService;

    public RegistrationService(AccountPort accountPort, LoyaltyCardService loyaltyCardService) {
        this.accountPort = accountPort;
        this.loyaltyCardService = loyaltyCardService;
    }

    // valide les informations et cree un compte client
    @Transactional
    public Account register(String firstName, String lastName, String email) {
        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()
                || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Les champs obligatoires doivent etre renseignes");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (!normalizedEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Adresse email invalide");
        }

        if (accountPort.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Cette adresse email est deja utilisee");
        }

        Account account = new Account(null, normalizedEmail, firstName.trim(),
                lastName.trim(), null, null, Role.CLIENT, UserStatus.ACTIVE, LocalDateTime.now());

        Account savedAccount = accountPort.save(account);

        // attribue automatiquement une carte numerique
        loyaltyCardService.issueCard(savedAccount.id(), false);

        return savedAccount;
    }
}
