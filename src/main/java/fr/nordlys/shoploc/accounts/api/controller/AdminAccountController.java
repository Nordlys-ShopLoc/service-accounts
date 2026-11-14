package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.service.AccountManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/accounts")
public class AdminAccountController {

    private final AccountManagementService accountManagementService;

    public AdminAccountController(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    @GetMapping
    public ResponseEntity<List<Account>> listAccounts(
            @RequestHeader("X-Admin-Id") Long adminId,
            @RequestParam(required = false) Role role) {
        return ResponseEntity.ok(accountManagementService.listAccounts(adminId, role));
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<Account> suspendAccount(
            @RequestHeader("X-Admin-Id") Long adminId,
            @PathVariable("id") Long targetAccountId) {
        return ResponseEntity.ok(accountManagementService.suspendAccount(adminId, targetAccountId));
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Account> reactivateAccount(
            @RequestHeader("X-Admin-Id") Long adminId,
            @PathVariable("id") Long targetAccountId) {
        return ResponseEntity.ok(accountManagementService.reactivateAccount(adminId, targetAccountId));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Account> changeRole(
            @RequestHeader("X-Admin-Id") Long adminId,
            @PathVariable("id") Long targetAccountId,
            @RequestParam Role newRole) {
        return ResponseEntity.ok(accountManagementService.changeRole(adminId, targetAccountId, newRole));
    }
}