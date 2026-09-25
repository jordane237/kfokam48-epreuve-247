package com.kfokam48.presence.config;

import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Random injectable : permet au service d'assignation d'être testé (aléatoire remplaçable). */
@Configuration
public class RandomConfig {

    @Bean
    public Random random() {
        return new Random();
    }
}
