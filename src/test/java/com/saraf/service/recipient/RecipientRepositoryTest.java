package com.saraf.service.recipient;

import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RecipientRepositoryTest {

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUserId() {
        User user = User.builder()
                .email("test@test.com")
                .firstname("Mohamed")
                .lastname("Test")
                .enabled(true)
                .build();
        user = userRepository.save(user); // Save and persist the user

        Recipient recipient = Recipient.builder().ccp("123").user(user).build();
        recipientRepository.save(recipient);

        var result = recipientRepository.findByUserId(user.getId());
        assertThat(result).isNotNull();
    }

    @Test
    public void testFindByUserIdAndAndCcp() {
        User user = User.builder()
                .email("test2@test.com")
                .firstname("Ahmed")
                .lastname("Test")
                .enabled(true)
                .build();
        user = userRepository.save(user); // Save and persist the user

        Recipient recipient = Recipient.builder().ccp("12345678901").user(user).build();
        recipientRepository.save(recipient);

        Recipient found = recipientRepository.findByUserIdAndAndCcp(user.getId(), "12345678901");
        assertNotNull(found);
    }

    @Test
    void findByCcp() {
        Recipient recipient = Recipient.builder()
                .firstname("Mohamed")
                .lastname("Zirek")
                .ccp("12345678901")
                .build();
        recipientRepository.save(recipient);

        var result = recipientRepository.findByCcp(recipient.getCcp());
        assertThat(result.get()).isEqualTo(recipient);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("test-auditor");
        }
    }
}
