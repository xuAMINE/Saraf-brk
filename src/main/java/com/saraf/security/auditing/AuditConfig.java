package com.saraf.security.auditing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;

@Configuration
//@Profile("test")
public class AuditConfig {

    @Bean
    public AuditorAware<Integer> auditorProvider() {
        return new ApplicationAuditAware();
    }
}

