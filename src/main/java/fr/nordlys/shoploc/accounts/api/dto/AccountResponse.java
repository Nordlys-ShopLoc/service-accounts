package fr.nordlys.shoploc.accounts.api.dto;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;

public record AccountResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String licensePlate,
        Role role,
        String status
) {
    public static AccountResponse fromDomain(Account account) {
        return new AccountResponse(
                account.id(), account.email(), account.firstName(),
                account.lastName(), account.phone(), account.licensePlate(),
                account.role(), account.status().name()
        );
    }
}