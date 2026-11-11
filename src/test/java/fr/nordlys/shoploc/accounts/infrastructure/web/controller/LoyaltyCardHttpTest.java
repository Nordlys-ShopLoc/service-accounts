package fr.nordlys.shoploc.accounts.infrastructure.web.controller;

import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardQueryService;
import fr.nordlys.shoploc.accounts.infrastructure.config.MethodSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

@WebMvcTest(LoyaltyCardController.class)
@Import({MethodSecurityConfiguration.class, LoyaltyCardExceptionHandler.class})
class LoyaltyCardHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoyaltyCardQueryService service;

    // refuse un utilisateur non connecte
    @Test
    void anonymousUserCannotScanCard() throws Exception {
        mockMvc.perform(get("/api/v1/cards/scan/QR123"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    // refuse un client qui n'a pas le role commercant
    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotScanCard() throws Exception {
        mockMvc.perform(get("/api/v1/cards/scan/QR123"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    // retourne 404 quand une carte n'existe pas
    @Test
    @WithMockUser(roles = "MERCHANT")
    void merchantGetsNotFoundForUnknownCard() throws Exception {
        when(service.getCardByQrCode("UNKNOWN"))
                .thenThrow(new IllegalArgumentException("Carte introuvable"));

        mockMvc.perform(get("/api/v1/cards/scan/UNKNOWN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // retourne 403 quand une carte est bloquee
    @Test
    @WithMockUser(roles = "MERCHANT")
    void merchantGetsForbiddenForBlockedCard() throws Exception {
        when(service.getCardByQrCode("BLOCKED"))
                .thenThrow(new IllegalStateException("Carte bloquee"));

        mockMvc.perform(get("/api/v1/cards/scan/BLOCKED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
