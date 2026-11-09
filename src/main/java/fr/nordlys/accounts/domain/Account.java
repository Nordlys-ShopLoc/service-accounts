package fr.nordlys.accounts.domain;

import java.time.LocalDateTime;

/**
 * A platform user, whatever their role.
 *
 * Domain object: no persistence annotation, no framework dependency.
 * Conversion to and from the database is handled by the adapter.
 */
public record Account(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String licensePlate,
        Role role,
        UserStatus status,
        LocalDateTime createdAt
) {

    public String fullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
