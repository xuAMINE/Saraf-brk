package com.saraf.security.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .password("password")
                .enabled(true)
                .build();
        userRepository.save(testUser);
    }

    @Test
    void testFindByEmail() {
        Optional<User> user = userRepository.findByEmail("johndoe@example.com");
        assertThat(user).isPresent();
        assertThat(user.get()).isEqualTo(testUser);
    }

    @Test
    void testFindById() {
        Optional<User> user = userRepository.findById(testUser.getId());
        assertThat(user).isPresent();
        assertThat(user.get()).isEqualTo(testUser);
    }

    @Test
    void testExistsByEmail() {
        boolean exists = userRepository.existsByEmail("johndoe@example.com");
        assertThat(exists).isTrue();

        exists = userRepository.existsByEmail("nonexistent@example.com");
        assertThat(exists).isFalse();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("test-auditor");
        }
    }
}
