package fr.nordlys.shoploc.accounts.infrastructure.web.controller;

import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardQueryService;
import fr.nordlys.shoploc.accounts.infrastructure.web.dto.LoyaltyCardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// permet au commercant de verifier une carte avec son qr code
@RestController
@RequestMapping("/api/v1/cards")
public class LoyaltyCardController {

    private final LoyaltyCardQueryService service;

    public LoyaltyCardController(LoyaltyCardQueryService service) {
        this.service = service;
    }

    // identifie une carte active a partir de son qr code
    @GetMapping("/scan/{qrCode}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<LoyaltyCardResponse> getCardByQrCode(@PathVariable String qrCode) {
        return ResponseEntity.ok(LoyaltyCardResponse.from(service.getCardByQrCode(qrCode)));
    }
}
