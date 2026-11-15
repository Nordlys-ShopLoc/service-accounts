package fr.nordlys.shoploc.accounts.infrastructure.config;

import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;
import fr.nordlys.shoploc.accounts.domain.port.OnlineRegistrationPort;
import fr.nordlys.shoploc.accounts.domain.port.PasswordHasher;
import fr.nordlys.shoploc.accounts.domain.port.TokenIssuer;
import fr.nordlys.shoploc.accounts.domain.service.AccountManagementService;
import fr.nordlys.shoploc.accounts.domain.service.AuthenticationService;
import fr.nordlys.shoploc.accounts.domain.service.InShopRegistrationService;
import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardQueryService;
import fr.nordlys.shoploc.accounts.domain.service.LoyaltyCardService;
import fr.nordlys.shoploc.accounts.domain.service.PersonalDataService;
import fr.nordlys.shoploc.accounts.domain.service.RegistrationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServicesConfiguration {

    @Bean
    LoyaltyCardService loyaltyCardService(LoyaltyCardPort cardPort) {
        return new LoyaltyCardService(cardPort);
    }

    // declare le service de consultation des cartes
    @Bean
    LoyaltyCardQueryService loyaltyCardQueryService(LoyaltyCardPort cardPort) {
        return new LoyaltyCardQueryService(cardPort);
    }

    // declare le service d'inscription en magasin
    @Bean
    InShopRegistrationService inShopRegistrationService(AccountPort accountPort,
                                                        LoyaltyCardService loyaltyCardService) {
        return new InShopRegistrationService(accountPort, loyaltyCardService);
    }

    @Bean
    AuthenticationService authenticationService(AccountPort accountPort, PasswordHasher passwordHasher,
                                                TokenIssuer tokenIssuer) {
        return new AuthenticationService(accountPort, passwordHasher, tokenIssuer);
    }

    @Bean
    AccountManagementService accountManagementService(AccountPort accountPort) {
        return new AccountManagementService(accountPort);
    }

    // declare le service d'inscription des clients
    @Bean
    RegistrationService registrationService(AccountPort accountPort, OnlineRegistrationPort registrationPort,
                                            LoyaltyCardService loyaltyCardService, PasswordHasher passwordHasher) {
        return new RegistrationService(accountPort, registrationPort, loyaltyCardService, passwordHasher);
    }

    @Bean
    PersonalDataService personalDataService(AccountPort accountPort, LoyaltyCardPort cardPort) {
        return new PersonalDataService(accountPort, cardPort);
    }
}
