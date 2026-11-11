package fr.nordlys.shoploc.accounts.infrastructure.web.controller;

import fr.nordlys.shoploc.accounts.infrastructure.config.RegistrationSecurityConfiguration;
import org.springframework.context.annotation.Import;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@Import(RegistrationSecurityConfiguration.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    // accepte une inscription sans connexion
    @Test
    void shouldRegisterCustomer() throws Exception {
        Account account = new Account(10L, "client@test.fr", "Marie", "Martin",
                null, null, Role.CLIENT, UserStatus.ACTIVE, LocalDateTime.now());

        when(registrationService.register("Marie", "Martin", "client@test.fr", "Password123"))
                .thenReturn(account);

        mockMvc.perform(post("/api/v1/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Marie",
                                  "lastName": "Martin",
                                  "email": "client@test.fr",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("client@test.fr"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    // refuse les informations invalides
    @Test
    void shouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "Martin",
                                  "email": "incorrect",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registrationService);
    }

    // refuse une adresse deja utilisee
    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        when(registrationService.register(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Cette adresse email est deja utilisee"));

        mockMvc.perform(post("/api/v1/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Marie",
                                  "lastName": "Martin",
                                  "email": "client@test.fr",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isConflict());
    }
}
