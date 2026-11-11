package fr.nordlys.shoploc.accounts.infrastructure.config;

import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;
import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardService;
import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
     // declare le service de consultation des cartes de fidelite
    @Bean
    LoyaltyCardQueryService loyaltyCardQueryService(LoyaltyCardPort cardPort) {
        return new LoyaltyCardQueryService(cardPort);
    }
}
