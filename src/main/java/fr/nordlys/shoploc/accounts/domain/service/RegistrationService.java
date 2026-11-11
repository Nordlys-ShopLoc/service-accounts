package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.OnlineRegistrationPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

// gere l'inscription en ligne des clients
public class RegistrationService {

    private final AccountPort accountPort;
    private final OnlineRegistrationPort registrationPort;
    private final LoyaltyCardService cardService;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(AccountPort accountPort, OnlineRegistrationPort registrationPort,
                               LoyaltyCardService cardService, PasswordEncoder passwordEncoder) {
        this.accountPort = accountPort;
        this.registrationPort = registrationPort;
        this.cardService = cardService;
        this.passwordEncoder = passwordEncoder;
    }

    // cree un compte client et sa carte de fidelite
    @Transactional
    public Account register(String firstName, String lastName, String email, String password) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()
                || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Les champs obligatoires doivent etre renseignes");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (!normalizedEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Adresse email invalide");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caracteres");
        }

        if (accountPort.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Cette adresse email est deja utilisee");
        }

        Account account = new Account(null, normalizedEmail, firstName.trim(), lastName.trim(),
                null, null, Role.CLIENT, UserStatus.ACTIVE, LocalDateTime.now());

        String passwordHash = passwordEncoder.encode(password);
        Account savedAccount = registrationPort.saveWithPassword(account, passwordHash);

        cardService.issueCard(savedAccount.id(), false);

        return savedAccount;
    }
}
