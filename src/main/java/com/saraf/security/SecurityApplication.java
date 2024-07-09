package com.saraf.security;

import com.saraf.security.auth.AuthenticationService;
import com.saraf.security.auth.RegisterRequest;
import com.saraf.security.user.Role;
import com.saraf.security.user.RoleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static com.saraf.security.user.Role.ADMIN;
import static com.saraf.security.user.Role.MANAGER;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class SecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			AuthenticationService service,
			RoleService roleService
	) {
		return args -> {
			var admin = RegisterRequest.builder()
					.firstname("Amine")
					.lastname("Zirek")
					.email("amine@saraf.com")
					.password("SarafBrk24$")
					.build();
			System.out.println("Admin token: " + service.register(admin).getAccessToken());

			var manager = RegisterRequest.builder()
					.firstname("Admin")
					.lastname("Admin")
					.email("manager@mail.com")
					.password("password")
					.build();
			System.out.println("Manager token: " + service.register(manager).getAccessToken());
			roleService.updateUserRole(1, ADMIN);

		};
	}
}
