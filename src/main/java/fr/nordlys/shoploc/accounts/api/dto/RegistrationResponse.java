package fr.nordlys.shoploc.accounts.api.dto;

// informations renvoyees apres inscription
public record RegistrationResponse(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}
