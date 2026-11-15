package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.api.dto.AccountResponse;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.service.AccountManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@PreAuthorize("hasRole('ADMIN')") // Protège toutes les routes de cette classe
public class AdminAccountController {

    private final AccountManagementService accountManagementService;

    public AdminAccountController(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAccounts(
            Authentication authentication,
            @RequestParam(required = false) Role role) {
        Long adminId = Long.valueOf(authentication.getName());
        List<AccountResponse> responses = accountManagementService.listAccounts(adminId, role)
                .stream().map(AccountResponse::fromDomain).toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<AccountResponse> suspendAccount(
            Authentication authentication,
            @PathVariable("id") Long targetAccountId) {
        Long adminId = Long.valueOf(authentication.getName());
        var account = accountManagementService.suspendAccount(adminId, targetAccountId);
        return ResponseEntity.ok(AccountResponse.fromDomain(account));
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<AccountResponse> reactivateAccount(
            Authentication authentication,
            @PathVariable("id") Long targetAccountId) {
        Long adminId = Long.valueOf(authentication.getName());
        var account = accountManagementService.reactivateAccount(adminId, targetAccountId);
        return ResponseEntity.ok(AccountResponse.fromDomain(account));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<AccountResponse> changeRole(
            Authentication authentication,
            @PathVariable("id") Long targetAccountId,
            @RequestParam Role newRole) {
        Long adminId = Long.valueOf(authentication.getName());
        var account = accountManagementService.changeRole(adminId, targetAccountId, newRole);
        return ResponseEntity.ok(AccountResponse.fromDomain(account));
    }
}