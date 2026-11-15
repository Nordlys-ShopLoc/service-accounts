package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardQueryService;
import fr.nordlys.shoploc.accounts.domain.exception.CardBlockedException;
import fr.nordlys.shoploc.accounts.domain.exception.CardNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

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
        ApiExceptionHandler handler = new ApiExceptionHandler();

        org.springframework.http.ProblemDetail response =
                handler.cardNotFound(new CardNotFoundException());

        assertEquals(HttpStatus.NOT_FOUND, HttpStatus.valueOf(response.getStatus()));
        assertEquals("Carte de fidélité introuvable", response.getDetail());
    }

    // verifie la reponse quand la carte est bloquee
    @Test
    void blockedCardReturnsForbidden() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        org.springframework.http.ProblemDetail response =
                handler.cardBlocked(new CardBlockedException());

        assertEquals(HttpStatus.FORBIDDEN, HttpStatus.valueOf(response.getStatus()));
        assertEquals("La carte de fidélité est bloquée", response.getDetail());
    }
}
