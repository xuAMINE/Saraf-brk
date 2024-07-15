package com.saraf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
public class SarafApplication {

	public static void main(String[] args) {
		SpringApplication.run(SarafApplication.class, args);
	}

	/*@Bean
	public CommandLineRunner commandLineRunner(
			AuthenticationService service,
			RoleService roleService
	) {
		return args -> {
			var admin = RegisterRequest.builder()
					.firstname("Amine")
					.lastname("Zirek")
					.email("uhammudz@gmail.com")
					.password("SarafBrk24$")
					.build();
			System.out.println("Admin token: " + service.register(admin).getAccessToken());

			roleService.updateUserRole(1, ADMIN);

		};
	}*/
}
