package fr.nordlys.shoploc.accounts.infrastructure.web.dto;

// informations renvoyees apres inscription
public record RegistrationResponse(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}
