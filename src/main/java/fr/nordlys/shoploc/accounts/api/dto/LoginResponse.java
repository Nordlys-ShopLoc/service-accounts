package fr.nordlys.shoploc.accounts.api.dto;

import java.time.Instant;

public record LoginResponse(String token, Instant expiresAt) {
}
