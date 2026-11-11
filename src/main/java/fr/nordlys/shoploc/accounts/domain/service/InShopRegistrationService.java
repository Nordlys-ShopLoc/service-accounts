package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.exception.EmailAlreadyUsedException;
import fr.nordlys.shoploc.accounts.domain.exception.OperationNotAllowedException;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;

/**
 * US-S16: a merchant registers a client in the shop and gives them a loyalty card.
 * The account has no password: the client does not use the application.
 */
public class InShopRegistrationService {

    private final AccountPort accountPort;
    private final LoyaltyCardService loyaltyCardService;

    public InShopRegistrationService(AccountPort accountPort, LoyaltyCardService loyaltyCardService) {
        this.accountPort = accountPort;
        this.loyaltyCardService = loyaltyCardService;
    }

    /**
     * Creates the client account and its card.
     * The e-mail and the phone are optional.
     */
    public LoyaltyCard registerClient(Long merchantId, String firstName, String lastName,
                                      String email, String phone) {
        checkIsActiveMerchant(merchantId);

        if (isBlank(firstName) || isBlank(lastName)) {
            throw new IllegalArgumentException("First name and last name are required");
        }

        // E-mails are saved in lower case so that "Pierre@mail.fr" and "pierre@mail.fr" are the same
        String cleanEmail = isBlank(email) ? null : email.trim().toLowerCase();
        String cleanPhone = isBlank(phone) ? null : phone.trim();

        if (cleanEmail != null && accountPort.existsByEmail(cleanEmail)) {
            throw new EmailAlreadyUsedException(cleanEmail);
        }

        Account client = new Account(null, cleanEmail, firstName.trim(), lastName.trim(),
                cleanPhone, null, Role.CLIENT, UserStatus.ACTIVE, null);
        Account savedClient = accountPort.save(client);

        return loyaltyCardService.issueCard(savedClient.id(), true);
    }

    private void checkIsActiveMerchant(Long merchantId) {
        Account merchant = accountPort.findById(merchantId).orElse(null);
        if (merchant == null || merchant.role() != Role.MERCHANT || !merchant.isActive()) {
            throw new OperationNotAllowedException("Only an active merchant can register a client in shop");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
