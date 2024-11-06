package com.saraf.service.rate;

import com.saraf.security.admin.ApiResponse;
import com.saraf.service.telegram.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rate")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private final TelegramBot telegramBot;

    @GetMapping
    public ResponseEntity<Double> getRate() {
        try {
            return ResponseEntity.ok(exchangeRateService.getCurrentRate());
        } catch (NullPointerException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRate(@RequestParam Integer newRate) {
        int oldRate = (int) exchangeRateService.getCurrentRate();

        if (newRate < 300 && newRate > 100){
            exchangeRateService.updateRate(newRate);
            telegramBot.sendNewRateToChannel(newRate, oldRate);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Rate was successfully updated", "" + newRate));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Rate was invalid", "" + newRate));
        }
    }

}
