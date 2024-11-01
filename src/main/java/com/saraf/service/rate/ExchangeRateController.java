package com.saraf.service.rate;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rate")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService ExchangeRateService;

    @GetMapping
    public ResponseEntity<Double> getRate() {
        try {
            return ResponseEntity.ok(ExchangeRateService.getCurrentRate());
        } catch (NullPointerException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRate(@RequestParam Integer newRate) {
        if (newRate < 300 && newRate > 100){
            ExchangeRateService.updateRate(newRate);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
