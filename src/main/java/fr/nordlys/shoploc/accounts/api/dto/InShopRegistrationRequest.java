package fr.nordlys.shoploc.accounts.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Form filled in by the merchant. E-mail and phone are optional. */
public record InShopRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email String email,
        String phone) {
}
