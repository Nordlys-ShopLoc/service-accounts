package fr.nordlys.shoploc.accounts.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class RegistrationSecurityConfiguration {

    // autorise uniquement l'inscription sans authentification
    @Bean
    @Order(1)
    SecurityFilterChain registrationSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/v1/accounts/register")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/accounts/register").permitAll()
                        .anyRequest().denyAll())
                .build();
    }
}
