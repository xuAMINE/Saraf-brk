package com.saraf.service.rate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository rateRepository;

    @InjectMocks
    private ExchangeRateService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCurrentRate_shouldReturnCurrentRateFromRepository() {
        // Arrange
        ExchangeRate expectedRate = new ExchangeRate();
        expectedRate.setCurrentRate(250.0);
        when(rateRepository.findTopByOrderByIdDesc()).thenReturn(expectedRate);

        // Act
        double actualRate = underTest.getCurrentRate();

        // Assert
        assertThat(actualRate).isEqualTo(250.0);
        verify(rateRepository).findTopByOrderByIdDesc();
    }

    @Test
    void updateRate_shouldSaveNewRateWhenValid() {
        // Arrange
        double newRate = 200.0;

        // Act
        underTest.updateRate(newRate);

        // Assert
        verify(rateRepository).save(any(ExchangeRate.class));
    }

    @Test
    void updateRate_shouldNotSaveNewRateWhenInvalid_low() {
        // Arrange
        double newRate = 90.0;

        // Act
        underTest.updateRate(newRate);

        // Assert
        verify(rateRepository, never()).save(any(ExchangeRate.class));
    }

    @Test
    void updateRate_shouldNotSaveNewRateWhenInvalid_high() {
        // Arrange
        double newRate = 310.0;

        // Act
        underTest.updateRate(newRate);

        // Assert
        verify(rateRepository, never()).save(any(ExchangeRate.class));
    }
}
