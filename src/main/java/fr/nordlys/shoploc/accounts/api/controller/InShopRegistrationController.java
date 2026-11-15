package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.api.dto.InShopRegistrationRequest;
import fr.nordlys.shoploc.accounts.api.dto.InShopRegistrationResponse;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.service.InShopRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/accounts")
public class InShopRegistrationController {

    private final InShopRegistrationService inShopRegistrationService;

    public InShopRegistrationController(InShopRegistrationService inShopRegistrationService) {
        this.inShopRegistrationService = inShopRegistrationService;
    }

    /**
     * US-S16: the logged-in merchant registers a client in the shop.
     * Transactional: if the card cannot be created, the account is not saved either.
     */
    @PostMapping("/in-shop")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public InShopRegistrationResponse register(@Valid @RequestBody InShopRegistrationRequest request,
                                               Authentication authentication) {
        LoyaltyCard card = inShopRegistrationService.registerClient(currentAccountId(authentication),
                request.firstName(), request.lastName(), request.email(), request.phone());
        return new InShopRegistrationResponse(card.accountId(), card.qrCode());
    }

    /** The id of the logged-in user is the subject of their JWT (see JwtTokenIssuer). */
    private Long currentAccountId(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        return Long.valueOf(authentication.getName());
    }
}
