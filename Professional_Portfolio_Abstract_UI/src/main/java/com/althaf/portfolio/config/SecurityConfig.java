package com.althaf.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/projects/**", "/about", "/contact/**",
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/favicon.ico"
                        ).permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
