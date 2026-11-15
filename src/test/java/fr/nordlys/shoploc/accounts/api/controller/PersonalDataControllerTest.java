package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.domain.exception.AccountNotFoundException;
import fr.nordlys.shoploc.accounts.domain.exception.CardBalanceNotEmptyException;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.service.PersonalDataService;
import fr.nordlys.shoploc.accounts.infrastructure.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-T02: HTTP routes /api/v1/accounts/me */
@WebMvcTest(PersonalDataController.class)
@Import(SecurityConfiguration.class)
class PersonalDataControllerTest {

    private static final String URL = "/api/v1/accounts/me";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonalDataService personalDataService;

    private final Account pierre = new Account(7L, "pierre@mail.fr", "Pierre", "Durand",
            "0611223344", "AB-123-CD", Role.CLIENT, UserStatus.ACTIVE, LocalDateTime.now());

    // username = the account id, like X-User-Id sent by the gateway
    @Test
    @WithMockUser(username = "7", roles = "CLIENT")
    void returns_my_data() throws Exception {
        when(personalDataService.getMyData(7L)).thenReturn(pierre);

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Pierre"))
                .andExpect(jsonPath("$.email").value("pierre@mail.fr"))
                .andExpect(jsonPath("$.licensePlate").value("AB-123-CD"));
    }

    @Test
    void answers_401_when_nobody_is_logged_in() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(personalDataService);
    }

    @Test
    @WithMockUser(username = "99", roles = "CLIENT")
    void answers_404_when_the_account_does_not_exist() throws Exception {
        when(personalDataService.getMyData(99L)).thenThrow(new AccountNotFoundException(99L));

        mockMvc.perform(get(URL))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "7", roles = "CLIENT")
    void updates_my_data() throws Exception {
        Account updated = new Account(7L, "pierre@mail.fr", "Pierre-Louis", "Durand",
                null, "XY-987-ZT", Role.CLIENT, UserStatus.ACTIVE, LocalDateTime.now());
        when(personalDataService.updateMyData(7L, "Pierre-Louis", "Durand", null, "XY-987-ZT"))
                .thenReturn(updated);

        mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Pierre-Louis", "lastName": "Durand", "licensePlate": "XY-987-ZT"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Pierre-Louis"))
                .andExpect(jsonPath("$.email").value("pierre@mail.fr"));
    }

    @Test
    @WithMockUser(username = "7", roles = "CLIENT")
    void answers_400_when_the_last_name_is_missing() throws Exception {
        mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Pierre"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(personalDataService);
    }

    @Test
    @WithMockUser(username = "7", roles = "CLIENT")
    void deletes_my_account() throws Exception {
        mockMvc.perform(delete(URL))
                .andExpect(status().isNoContent());

        verify(personalDataService).deleteMyAccount(7L);
    }

    @Test
    @WithMockUser(username = "7", roles = "CLIENT")
    void answers_409_when_money_is_left_on_the_card() throws Exception {
        doThrow(new CardBalanceNotEmptyException(new BigDecimal("12.50")))
                .when(personalDataService).deleteMyAccount(7L);

        mockMvc.perform(delete(URL))
                .andExpect(status().isConflict());
    }
}
