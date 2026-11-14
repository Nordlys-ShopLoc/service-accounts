package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.api.dto.LoginRequest;
import fr.nordlys.shoploc.accounts.api.dto.LoginResponse;
import fr.nordlys.shoploc.accounts.domain.model.AuthenticationToken;
import fr.nordlys.shoploc.accounts.domain.service.AuthenticationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        AuthenticationToken token = authenticationService.login(request.email(), request.password());
        return new LoginResponse(token.value(), token.expiresAt());
    }
}
