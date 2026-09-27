package com.tigo.shop.order;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class OrderBeansConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
