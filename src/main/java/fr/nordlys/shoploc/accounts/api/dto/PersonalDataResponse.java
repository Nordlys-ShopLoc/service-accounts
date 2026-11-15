package fr.nordlys.shoploc.accounts.api.dto;

import fr.nordlys.shoploc.accounts.domain.model.Account;

/** Personal data shown to the logged-in user. */
public record PersonalDataResponse(Long id, String firstName, String lastName,
                                   String email, String phone, String licensePlate) {

    public static PersonalDataResponse from(Account account) {
        return new PersonalDataResponse(account.id(), account.firstName(), account.lastName(),
                account.email(), account.phone(), account.licensePlate());
    }
}
