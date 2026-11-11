package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.domain.exception.AccountNotActiveException;
import fr.nordlys.shoploc.accounts.domain.exception.InvalidCredentialsException;
import fr.nordlys.shoploc.accounts.domain.model.AuthenticationToken;
import fr.nordlys.shoploc.accounts.domain.service.AuthenticationService;
import fr.nordlys.shoploc.accounts.infrastructure.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    private ResultActions login() throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "julie@mail.fr", "password": "secret"}
                        """));
    }

    @Test
    void returns_the_token_when_the_credentials_are_valid() throws Exception {
        when(authenticationService.login("julie@mail.fr", "secret"))
                .thenReturn(new AuthenticationToken("jwt", Instant.parse("2026-11-12T10:00:00Z")));

        login().andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"));
    }

    @Test
    void answers_401_when_the_credentials_are_invalid() throws Exception {
        when(authenticationService.login("julie@mail.fr", "secret"))
                .thenThrow(new InvalidCredentialsException());

        login().andExpect(status().isUnauthorized());
    }

    @Test
    void answers_403_when_the_account_is_not_active() throws Exception {
        when(authenticationService.login("julie@mail.fr", "secret"))
                .thenThrow(new AccountNotActiveException());

        login().andExpect(status().isForbidden());
    }
}
