package fr.nordlys.shoploc.accounts.api.dto;

import jakarta.validation.constraints.NotBlank;

/** New personal data. The e-mail cannot be changed. */
public record PersonalDataUpdateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String phone,
        String licensePlate) {
}
