package com.saraf.service.rate;

import com.saraf.security.config.JwtService;
import com.saraf.security.config.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Test
    void getRate_shouldReturnCurrentRate() throws Exception {
        double rate = 250.0;
        when(exchangeRateService.getCurrentRate()).thenReturn(rate);

        mockMvc.perform(get("/api/v1/rate"))
                .andExpect(status().isOk())
                .andExpect(content().string("250.0"));
    }

    @Test
    void getRate_shouldReturnNotFoundWhenServiceThrowsException() throws Exception {
        when(exchangeRateService.getCurrentRate()).thenThrow(new NullPointerException());

        mockMvc.perform(get("/api/v1/rate"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRate_shouldUpdateRateWhenValid() throws Exception {
        double newRate = 200.0;
        doNothing().when(exchangeRateService).updateRate(newRate);

        mockMvc.perform(post("/api/v1/rate")
                .param("newRate", "200")) // Pass newRate as request parameter
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRate_shouldReturnBadRequestWhenInvalid_low() throws Exception {
        mockMvc.perform(post("/api/v1/rate")
                        .param("newRate", "90")) // Pass invalid newRate (too low) as request parameter
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRate_shouldReturnBadRequestWhenInvalid_high() throws Exception {
        mockMvc.perform(post("/api/v1/rate")
                        .param("newRate", "310")) // Pass invalid newRate (too high) as request parameter
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRate_shouldReturnUnauthorizedForNonAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/rate")
                        .param("newRate", "200")) // Pass newRate as request parameter without admin role
                .andExpect(status().isForbidden());
    }
}
