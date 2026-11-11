package fr.nordlys.shoploc.accounts.infrastructure.web.controller;

import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardQueryService;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.model.CardStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.*;

@WebMvcTest(LoyaltyCardController.class)
@Import({MethodSecurityConfiguration.class, LoyaltyCardExceptionHandler.class})
class LoyaltyCardHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoyaltyCardQueryService service;

    @MockitoBean
    private AccountPort accountPort;

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

    // verifie qu'un commercant peut identifier une carte active
    @Test
    @WithMockUser(roles = "MERCHANT")
    void merchantCanScanActiveCard() throws Exception {
        LoyaltyCard card = new LoyaltyCard(1L, 10L, "QR123", BigDecimal.ZERO, false, CardStatus.ACTIVE, LocalDateTime.now());

        when(service.getCardByQrCode("QR123")).thenReturn(card);

        mockMvc.perform(get("/api/v1/cards/scan/QR123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.qrCode").value("QR123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(service).getCardByQrCode("QR123");
    }

    // verifie qu'un client connecte peut consulter sa carte
    @Test
    @WithMockUser(username = "client@test.fr", roles = "CLIENT")
    void clientCanGetOwnCard() throws Exception {
        Account account = new Account(10L, "client@test.fr", "Marie", "Martin",
                null, null, Role.CLIENT, UserStatus.ACTIVE, LocalDateTime.now());

        LoyaltyCard card = new LoyaltyCard(1L, 10L, "QR123",
                BigDecimal.ZERO, false, CardStatus.ACTIVE, LocalDateTime.now());

        when(accountPort.findByEmail("client@test.fr")).thenReturn(java.util.Optional.of(account));
        when(service.getCardByAccountId(10L)).thenReturn(card);

        mockMvc.perform(get("/api/v1/cards/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode").value("QR123"));

        verify(service).getCardByAccountId(10L);
    }

    // refuse un commercant sur la route reservee aux clients
    @Test
    @WithMockUser(roles = "MERCHANT")
    void merchantCannotAccessClientCard() throws Exception {
        mockMvc.perform(get("/api/v1/cards/me"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(accountPort);
    }
}
