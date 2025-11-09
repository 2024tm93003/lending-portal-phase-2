package com.school.lending.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Application CORS configuration.
 *
 * <p>Defines CORS rules and exposes a {@link CorsFilter} bean so the
 * application can accept cross-origin requests according to these rules.
 */
@Configuration
public class CorsConfig {

    /**
     * Build and register the application's CORS configuration.
     *
     * @return a {@link UrlBasedCorsConfigurationSource} containing the
     * configured CORS mappings for API and root endpoints
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowCredentials(false);
        cfg.setAllowedOriginPatterns(Arrays.asList("*"));
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(Arrays.asList("*"));
        cfg.setExposedHeaders(Arrays.asList("X-Auth-Token"));
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        source.registerCorsConfiguration("/**", cfg);
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
