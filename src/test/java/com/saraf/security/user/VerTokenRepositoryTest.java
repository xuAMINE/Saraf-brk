package com.saraf.security.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataJpaTest
class VerTokenRepositoryTest {

    @Autowired
    private VerTokenRepository verTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private VerificationToken validToken;
    private VerificationToken expiredToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);

        validToken = new VerificationToken();
        validToken.setToken("validToken");
        validToken.setUser(testUser);
        validToken.setExpires(LocalDateTime.now().plusDays(1));
        verTokenRepository.save(validToken);

        expiredToken = new VerificationToken();
        expiredToken.setToken("expiredToken");
        expiredToken.setUser(testUser);
        expiredToken.setExpires(LocalDateTime.now().minusDays(1));
        verTokenRepository.save(expiredToken);
    }

    @Test
    void testFindByToken_Success() {
        Optional<VerificationToken> token = verTokenRepository.findByToken("validToken");
        assertThat(token).isPresent();
        assertThat(token.get()).isEqualTo(validToken);
    }

    @Test
    void testFindAllActiveTokensByUser_Success() {
        List<VerificationToken> activeTokens = verTokenRepository.findAllActiveTokensByUser(testUser.getId());
        assertThat(activeTokens).hasSize(1);
        assertThat(activeTokens.get(0)).isEqualTo(validToken);
    }

    @Test
    void testFindByToken_TokenNotFound() {
        Optional<VerificationToken> token = verTokenRepository.findByToken("nonExistentToken");
        assertThat(token).isNotPresent();
    }

    @Test
    void testFindAllActiveTokensByUser_NoActiveTokens() {
        List<VerificationToken> activeTokens = verTokenRepository.findAllActiveTokensByUser(testUser.getId());
        assertThat(activeTokens).hasSize(1); // Only the valid token should be returned
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("test-auditor");
        }
    }
}
