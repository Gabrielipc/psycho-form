package com.uam.psychoform.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CoreConfiguration {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
