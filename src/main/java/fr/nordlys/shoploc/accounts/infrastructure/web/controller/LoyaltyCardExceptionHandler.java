package fr.nordlys.shoploc.accounts.infrastructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// transforme les erreurs de consultation en reponses http
@RestControllerAdvice(assignableTypes = LoyaltyCardController.class)
public class LoyaltyCardExceptionHandler {

    // retourne 404 si la carte n'existe pas
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> cardNotFound(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }

    // retourne 403 si la carte est bloquee
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> cardBlocked(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", exception.getMessage()));
    }
}
