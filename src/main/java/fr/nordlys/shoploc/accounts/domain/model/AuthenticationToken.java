package fr.nordlys.shoploc.accounts.domain.model;

import java.time.Instant;

public record AuthenticationToken(String value, Instant expiresAt) {
}
