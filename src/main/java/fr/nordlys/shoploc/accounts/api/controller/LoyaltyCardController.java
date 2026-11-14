package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardQueryService;
import fr.nordlys.shoploc.accounts.api.dto.LoyaltyCardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// permet de consulter une carte de fidelite
@RestController
@RequestMapping("/api/v1/cards")
public class LoyaltyCardController {

    private final LoyaltyCardQueryService service;

    public LoyaltyCardController(LoyaltyCardQueryService service) {
        this.service = service;
    }

    // recupere la carte du client connecte
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<LoyaltyCardResponse> getMyCard(Authentication authentication) {
        Long accountId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(LoyaltyCardResponse.from(service.getCardByAccountId(accountId)));
    }

    // identifie une carte a partir du qr code
    @GetMapping("/scan/{qrCode}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<LoyaltyCardResponse> getCardByQrCode(@PathVariable String qrCode) {
        return ResponseEntity.ok(LoyaltyCardResponse.from(service.getCardByQrCode(qrCode)));
    }
}
