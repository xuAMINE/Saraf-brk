package com.saraf;

import com.saraf.security.auth.AuthenticationService;
import com.saraf.security.auth.RegisterRequest;
import com.saraf.security.user.Role;
import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import com.saraf.service.rate.ExchangeRateService;
import com.saraf.service.recipient.RecipientRequest;
import com.saraf.service.recipient.RecipientService;
import com.saraf.service.transfer.TransferRequest;
import com.saraf.service.transfer.TransferService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;

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
			RecipientService recipientService,
			AuthenticationManager authenticationManager,
			UserRepository userRepository,
			TransferService transferService,
			ExchangeRateService exchangeRateService
	) {
		return args -> {
			var admin = RegisterRequest.builder()
					.firstname("Amine")
					.lastname("Zirek")
					.email("amine@zirek.com")
					.password("SarafBrk")
					.build();
			System.out.println("Admin token: " + service.register(admin).getAccessToken());

					User user = userRepository.findByEmail(admin.getEmail()).orElseThrow();
			user.setEnabled(true);
			user.setRole(Role.ADMIN);
			userRepository.save(user);

			// Authenticate the admin user m anually
			UsernamePasswordAuthenticationToken authToken =
					new UsernamePasswordAuthenticationToken(admin.getEmail(), admin.getPassword());
			Authentication authentication = authenticationManager.authenticate(authToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);

			var recipient = RecipientRequest.builder()
					.firstName("NedjmEdinne")
					.lastName("Zirek")
					.ccp("002938457589")
					.phoneNumber("0548274598")
					.doContact(true)
					.build();
			recipientService.addRecipient(recipient);

			exchangeRateService.updateRate(215);

			var transfer = TransferRequest.builder()
					.amount(BigDecimal.valueOf(400))
					.ccp("002938457589")
					.build();
			transferService.addTransfer(transfer);

			SecurityContextHolder.clearContext();
		};
	}
}
