package fr.nordlys.shoploc.accounts.domain.model;

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

    public boolean isDeleted() {
        return status == UserStatus.DELETED;
    }

    public Account withStatus(UserStatus newStatus) {
        return new Account(id, email, firstName, lastName, phone, licensePlate, role, newStatus, createdAt);
    }

    public Account withRole(Role newRole) {
        return new Account(id, email, firstName, lastName, phone, licensePlate, newRole, status, createdAt);
    }
}
