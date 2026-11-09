package fr.nordlys.shoploc.accounts;

import org.springframework.boot.SpringApplication;

public class TestServiceAccountsApplication {

	public static void main(String[] args) {
		SpringApplication.from(ServiceAccountsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
