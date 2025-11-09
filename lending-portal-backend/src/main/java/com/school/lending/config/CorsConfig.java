package com.school.lending.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Application CORS configuration.
 *
 * <p>Defines CORS rules and exposes a {@link CorsFilter} bean so the
 * application can accept cross-origin requests according to these rules.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    /**
     * Build and register the application's CORS configuration.
     *
     * @return a {@link UrlBasedCorsConfigurationSource} containing the
     * configured CORS mappings for API and root endpoints
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // Read allowed origins from application properties; default to localhost:3000
        List<String> allowed = Arrays.asList(allowedOrigins.split(","));
        cfg.setAllowCredentials(true);
        cfg.setAllowedOrigins(allowed);
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(Arrays.asList("*"));
        cfg.setExposedHeaders(Arrays.asList("X-Auth-Token"));
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        // Keep CORS limited to API paths; avoid exposing internal endpoints by default.
        source.registerCorsConfiguration("/", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    /**
     * Create a {@link CorsFilter} that enforces the CORS configuration built
     * by {@link #corsConfigurationSource()}.
     *
     * @param source the CORS configuration source
     * @return a CorsFilter instance
     */
    @Bean
    public CorsFilter corsFilter(UrlBasedCorsConfigurationSource source) {
        return new CorsFilter(source);
    }
}
