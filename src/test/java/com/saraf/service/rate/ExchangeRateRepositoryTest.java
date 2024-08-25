package com.saraf.service.rate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import(ExchangeRateRepositoryTest.TestConfig.class)
@EnableJpaRepositories(basePackageClasses = ExchangeRateRepository.class) // Ensure repository scanning
class ExchangeRateRepositoryTest {

    @Autowired
    private ExchangeRateRepository underTest;

    @Test
    void findTopByOrderByIdDesc() {
        ExchangeRate rate = new ExchangeRate(1, 250);
        underTest.save(rate);

        ExchangeRate result = underTest.findTopByOrderByIdDesc();

        assertThat(result).isEqualTo(rate);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("test-auditor");
        }
    }
}
