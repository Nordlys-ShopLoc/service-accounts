package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.api.dto.AccountResponse;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.service.AccountManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AdminAccountController {

    private final AccountManagementService accountManagementService;

    public AdminAccountController(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAccounts(
            @RequestHeader("X-User-Id") Long adminId,
            @RequestParam(required = false) Role role) {

        List<AccountResponse> responses = accountManagementService.listAccounts(adminId, role)
                .stream()
                .map(AccountResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<AccountResponse> suspendAccount(
            @RequestHeader("X-User-Id") Long adminId,
            @PathVariable("id") Long targetAccountId) {

        var account = accountManagementService.suspendAccount(adminId, targetAccountId);
        return ResponseEntity.ok(AccountResponse.fromDomain(account));
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<AccountResponse> reactivateAccount(
            @RequestHeader("X-User-Id") Long adminId,
            @PathVariable("id") Long targetAccountId) {

        var account = accountManagementService.reactivateAccount(adminId, targetAccountId);
        return ResponseEntity.ok(AccountResponse.fromDomain(account));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<AccountResponse> changeRole(
            @RequestHeader("X-User-Id") Long adminId,
            @PathVariable("id") Long targetAccountId,
            @RequestParam Role newRole) {

        var account = accountManagementService.changeRole(adminId, targetAccountId, newRole);
        return ResponseEntity.ok(AccountResponse.fromDomain(account));
    }
}