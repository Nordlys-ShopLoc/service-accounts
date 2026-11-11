package fr.nordlys.shoploc.accounts.infrastructure.config;

import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.OnlineRegistrationPort;
import fr.nordlys.shoploc.accounts.domain.service.RegistrationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.service.RegistrationService;
import fr.nordlys.shoploc.accounts.domain.service.InShopRegistrationService;
import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Declares the domain services as Spring beans.
 * The domain has no Spring annotation (see README), so services are created here.
 */
@Configuration
public class DomainServicesConfiguration {

    @Bean
    LoyaltyCardService loyaltyCardService(LoyaltyCardPort cardPort) {
        return new LoyaltyCardService(cardPort);
    }

    @Bean
    InShopRegistrationService inShopRegistrationService(AccountPort accountPort,
                                                        LoyaltyCardService loyaltyCardService) {
        return new InShopRegistrationService(accountPort, loyaltyCardService);
    }

    // declare le service d'inscription des clients
    @Bean
    RegistrationService registrationService(AccountPort accountPort, OnlineRegistrationPort registrationPort,
                                            LoyaltyCardService loyaltyCardService, PasswordEncoder passwordEncoder) {
        return new RegistrationService(accountPort, registrationPort, loyaltyCardService, passwordEncoder);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
