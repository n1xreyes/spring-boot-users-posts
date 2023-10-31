package com.angelo.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api-docs/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/webjars/**")).permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers ->
                        headers
                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}