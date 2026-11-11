package fr.nordlys.shoploc.accounts.infrastructure.web.controller;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardQueryService;
import fr.nordlys.shoploc.accounts.infrastructure.web.dto.LoyaltyCardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// permet de consulter une carte de fidelite
@RestController
@RequestMapping("/api/v1/cards")
public class LoyaltyCardController {

    private final LoyaltyCardQueryService service;
    private final AccountPort accountPort;

    public LoyaltyCardController(LoyaltyCardQueryService service, AccountPort accountPort) {
        this.service = service;
        this.accountPort = accountPort;
    }

    // recupere la carte du client connecte
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<LoyaltyCardResponse> getMyCard(Authentication authentication) {
        Account account = accountPort.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));

        return ResponseEntity.ok(LoyaltyCardResponse.from(service.getCardByAccountId(account.id())));
    }

    // identifie une carte a partir du qr code
    @GetMapping("/scan/{qrCode}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<LoyaltyCardResponse> getCardByQrCode(@PathVariable String qrCode) {
        return ResponseEntity.ok(LoyaltyCardResponse.from(service.getCardByQrCode(qrCode)));
    }
}
