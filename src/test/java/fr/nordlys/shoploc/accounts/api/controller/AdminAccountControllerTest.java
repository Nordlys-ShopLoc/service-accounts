package fr.nordlys.shoploc.accounts.api.controller;

import fr.nordlys.shoploc.accounts.TestcontainersConfiguration;
import fr.nordlys.shoploc.accounts.domain.service.AccountManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AdminAccountControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AccountManagementService accountManagementService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void givenAdminUser_whenListAccounts_thenReturns200Ok() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "2", roles = "CLIENT")
    void givenClientUser_whenListAccounts_thenReturns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isForbidden());
    }
}