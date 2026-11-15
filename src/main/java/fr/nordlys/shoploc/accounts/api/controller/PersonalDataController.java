package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.api.dto.PersonalDataResponse;
import fr.nordlys.shoploc.accounts.api.dto.PersonalDataUpdateRequest;
import fr.nordlys.shoploc.accounts.domain.service.PersonalDataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * US-T02: the logged-in user consults, modifies or deletes their personal data.
 * "me" = the account id comes from the token, so nobody can reach another user's data.
 */
@RestController
@RequestMapping("/api/v1/accounts/me")
public class PersonalDataController {

    private final PersonalDataService personalDataService;

    public PersonalDataController(PersonalDataService personalDataService) {
        this.personalDataService = personalDataService;
    }

    @GetMapping
    public PersonalDataResponse getMyData(Authentication authentication) {
        return PersonalDataResponse.from(personalDataService.getMyData(currentAccountId(authentication)));
    }

    @PutMapping
    public PersonalDataResponse updateMyData(@Valid @RequestBody PersonalDataUpdateRequest request,
                                             Authentication authentication) {
        return PersonalDataResponse.from(personalDataService.updateMyData(currentAccountId(authentication),
                request.firstName(), request.lastName(), request.phone(), request.licensePlate()));
    }

    /** Transactional: the account and the card are changed together or not at all. */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteMyAccount(Authentication authentication) {
        personalDataService.deleteMyAccount(currentAccountId(authentication));
    }

    /** The id of the logged-in user, sent by the gateway in X-User-Id. */
    private Long currentAccountId(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        return Long.valueOf(authentication.getName());
    }
}
