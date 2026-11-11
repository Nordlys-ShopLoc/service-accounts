package fr.nordlys.shoploc.accounts.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// active la verification des roles sur les methodes des controleurs
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfiguration {
}
