package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.domain.exception.EmailAlreadyUsedException;
import fr.nordlys.shoploc.accounts.domain.exception.OperationNotAllowedException;
import fr.nordlys.shoploc.accounts.domain.model.CardStatus;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.service.InShopRegistrationService;
import fr.nordlys.shoploc.accounts.infrastructure.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-S16: HTTP route POST /api/v1/accounts/in-shop */
@WebMvcTest(InShopRegistrationController.class)
@Import(SecurityConfiguration.class)
class InShopRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InShopRegistrationService inShopRegistrationService;

    private ResultActions register(String json) throws Exception {
        return mockMvc.perform(post("/api/v1/accounts/in-shop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    // username = the account id, like the subject of the JWT
    @Test
    @WithMockUser(username = "1", roles = "MERCHANT")
    void returns_the_new_client_and_the_qr_code_of_the_card() throws Exception {
        LoyaltyCard card = new LoyaltyCard(10L, 2L, "SL-ABC", BigDecimal.ZERO,
                true, CardStatus.ACTIVE, LocalDateTime.now());
        when(inShopRegistrationService.registerClient(1L, "Pierre", "Durand", null, null)).thenReturn(card);

        register("""
                {"firstName": "Pierre", "lastName": "Durand"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(2))
                .andExpect(jsonPath("$.qrCode").value("SL-ABC"));
    }

    @Test
    @WithMockUser(username = "1", roles = "MERCHANT")
    void answers_409_when_the_email_is_already_used() throws Exception {
        when(inShopRegistrationService.registerClient(any(), any(), any(), any(), any()))
                .thenThrow(new EmailAlreadyUsedException("pierre@mail.fr"));

        register("""
                {"firstName": "Pierre", "lastName": "Durand", "email": "pierre@mail.fr"}
                """)
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "5", roles = "CLIENT")
    void answers_403_when_the_user_is_not_an_active_merchant() throws Exception {
        when(inShopRegistrationService.registerClient(eq(5L), any(), any(), any(), any()))
                .thenThrow(new OperationNotAllowedException("Only an active merchant can register a client in shop"));

        register("""
                {"firstName": "Pierre", "lastName": "Durand"}
                """)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = "MERCHANT")
    void answers_400_when_the_last_name_is_missing() throws Exception {
        register("""
                {"firstName": "Pierre"}
                """)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(inShopRegistrationService);
    }

    @Test
    void answers_401_when_nobody_is_logged_in() throws Exception {
        register("""
                {"firstName": "Pierre", "lastName": "Durand"}
                """)
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(inShopRegistrationService);
    }
}
