package com.kfokam48.presence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS : autorise le frontend Angular en développement (http://localhost:4200)
 * à appeler l'API depuis un port différent. Sans cela, le démarrage
 * front + back ne fonctionne pas pour le correcteur.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT")
                .allowedHeaders("*");
    }
}
