package com.saraf;

import com.saraf.security.auth.AuthenticationService;
import com.saraf.security.auth.RegisterRequest;
import com.saraf.security.user.RoleService;
import com.saraf.service.recipient.Recipient;
import com.saraf.service.recipient.RecipientRequest;
import com.saraf.service.recipient.RecipientService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
public class SarafApplication {

	public static void main(String[] args) {
		SpringApplication.run(SarafApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			AuthenticationService service,
			RecipientService recipientService
	) {
		return args -> {
			var admin = RegisterRequest.builder()
					.firstname("Amine")
					.lastname("Zirek")
					.email("uhammu@gmail.com")
					.password("SarafBrk24$")
					.build();
			System.out.println("Admin token: " + service.register(admin).getAccessToken());

			var recipient = RecipientRequest.builder()
					.firstName("NedjmEdinne")
					.lastName("Zirek")
					.ccp("000000798234e8776")
					.phoneNumber("0548274598")
					.doContact(true)
					.build();
//			recipientService.addRecipient(recipient);
		};
	}
}
