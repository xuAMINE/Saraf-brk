// src/test/java/com/saraf/security/token/TokenRepositoryTest.java
package com.saraf.security.token;

import static org.assertj.core.api.Assertions.assertThat;

import com.saraf.security.user.User;
import com.saraf.security.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@Import(TokenRepositoryTest.TestConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class TokenRepositoryTest {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Token validToken;
    private Token revokedExpToken;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setEmail("test@user.com");
        testUser = userRepository.save(testUser);

        // Create tokens
        validToken = new Token();
        validToken.setToken("validToken");
        validToken.setExpired(false);
        validToken.setRevoked(false);
        validToken.setUser(testUser);
        tokenRepository.save(validToken);

        Token expiredToken = new Token();
        expiredToken.setUser(testUser);
        expiredToken.setToken("expiredToken");
        expiredToken.setExpired(true);
        expiredToken.setRevoked(false);
        tokenRepository.save(expiredToken);

        Token revokedToken = new Token();
        revokedToken.setUser(testUser);
        revokedToken.setToken("revokedToken");
        revokedToken.setExpired(false);
        revokedToken.setRevoked(true);
        tokenRepository.save(revokedToken);

        revokedExpToken = new Token();
        revokedExpToken.setUser(testUser);
        revokedExpToken.setToken("revokedExpiredToken");
        revokedExpToken.setExpired(true);
        revokedExpToken.setRevoked(true);
        tokenRepository.save(revokedExpToken);
    }

    @Test
    void testFindAllValidTokenByUser() {
        // Given
        Integer userId = testUser.getId();

        // When
        List<Token> validTokens = tokenRepository.findAllValidTokenByUser(userId);

        // Then
        assertThat(validTokens).hasSize(3);
        assertThat(validTokens).contains(validToken);
        assertThat(validTokens).doesNotContain(revokedExpToken);
    }

    @Test
    void testFindByToken() {
        // When
        Optional<Token> token = tokenRepository.findByToken("validToken");

        // Then
        assertThat(token).isPresent();
        assertThat(token.get()).isEqualTo(validToken);

        // When
        Optional<Token> nonExistentToken = tokenRepository.findByToken("nonExistentToken");

        // Then
        assertThat(nonExistentToken).isNotPresent();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("test-auditor");
        }
    }
}