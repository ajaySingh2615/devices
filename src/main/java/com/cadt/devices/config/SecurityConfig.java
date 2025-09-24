package com.cadt.devices.config;

import com.cadt.devices.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())           // No CSRF for pure JWT API
                .cors(cors -> {
                })                      // OK to leave default
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public infra & docs
                        .requestMatchers("/", "/error").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-resources/**").permitAll()

                        // auth endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/super-admin/**").permitAll()

                        // public catalog endpoints
                        .requestMatchers("/api/v1/categories/**").permitAll()
                        .requestMatchers("/api/v1/brands/**").permitAll()
                        .requestMatchers("/api/v1/products/**").permitAll()

                        // public reviews read-only endpoints
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/reviews/product/**").permitAll()

                        // newsletter subscribe public endpoint
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/newsletter/subscribe").permitAll()

                        // cart endpoints (support both authenticated and anonymous users)
                        .requestMatchers("/api/v1/cart/**").permitAll()

                        // preflight
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // admin
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // everything else
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
