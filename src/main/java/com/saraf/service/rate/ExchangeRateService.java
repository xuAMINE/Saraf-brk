package com.saraf.service.rate;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository rateRepository;

    public double getCurrentRate() {
        return rateRepository.findTopByOrderByIdDesc().getCurrentRate();
    }

    public void updateRate(double newRate) {
        if (newRate < 300 && newRate > 100) {
            ExchangeRate exchangeRate = new ExchangeRate();
            exchangeRate.setCurrentRate(newRate);
            rateRepository.save(exchangeRate);
        }
    }

}
