package fr.nordlys.shoploc.accounts.infrastructure.web.controller;

import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoyaltyCardControllerTest {

    private LoyaltyCardQueryService service;
    private LoyaltyCardController controller;

    @BeforeEach
    void setUp() {
        service = mock(LoyaltyCardQueryService.class);
        controller = new LoyaltyCardController(service);
    }

    // verifie que la route de scan est reservee aux commercants
    @Test
    void scanRequiresMerchantRole() throws Exception {
        Method method = LoyaltyCardController.class.getMethod("getCardByQrCode", String.class);
        PreAuthorize security = method.getAnnotation(PreAuthorize.class);

        assertNotNull(security);
        assertEquals("hasRole('MERCHANT')", security.value());
    }

    // verifie la reponse quand la carte est introuvable
    @Test
    void missingCardReturnsNotFound() {
        LoyaltyCardExceptionHandler handler = new LoyaltyCardExceptionHandler();

        ResponseEntity<Map<String, String>> response =
                handler.cardNotFound(new IllegalArgumentException("Carte introuvable"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Carte introuvable", response.getBody().get("error"));
    }

    // verifie la reponse quand la carte est bloquee
    @Test
    void blockedCardReturnsForbidden() {
        LoyaltyCardExceptionHandler handler = new LoyaltyCardExceptionHandler();

        ResponseEntity<Map<String, String>> response =
                handler.cardBlocked(new IllegalStateException("Carte bloquee"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Carte bloquee", response.getBody().get("error"));
    }
}
