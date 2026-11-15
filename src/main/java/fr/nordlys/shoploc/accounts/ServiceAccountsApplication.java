package fr.nordlys.shoploc.accounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class ServiceAccountsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceAccountsApplication.class, args);
	}

}
