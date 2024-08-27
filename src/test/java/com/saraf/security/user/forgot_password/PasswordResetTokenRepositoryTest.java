package com.saraf.security.user.forgot_password;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;

@DataJpaTest
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private PasswordResetToken passwordResetToken;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail("test@user.com");
        testUser = userRepository.save(testUser);

        passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken("validToken");
        passwordResetToken.setExpirationTime(LocalDateTime.now().plusMinutes(15));
        passwordResetToken.setUser(testUser);

        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Test
    void testFindByToken_WhenTokenExists() {
        Optional<PasswordResetToken> foundToken = passwordResetTokenRepository.findByToken("validToken");
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get()).isEqualTo(passwordResetToken);
    }

    @Test
    void testFindByToken_WhenTokenDoesNotExist() {
        Optional<PasswordResetToken> foundToken = passwordResetTokenRepository.findByToken("nonExistentToken");
        assertThat(foundToken).isNotPresent();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("test-auditor");
        }
    }
}
