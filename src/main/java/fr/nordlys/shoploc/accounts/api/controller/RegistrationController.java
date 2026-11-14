package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.service.RegistrationService;
import fr.nordlys.shoploc.accounts.api.dto.RegistrationRequest;
import fr.nordlys.shoploc.accounts.api.dto.RegistrationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    // inscrit un client et cree sa carte de fidelite
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        Account account = registrationService.register(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password()
        );

        return new RegistrationResponse(
                account.id(),
                account.email(),
                account.firstName(),
                account.lastName()
        );
    }
}
