package com.nines.nutsfact.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/**").permitAll()
                .requestMatchers("/apix/Auth/signUp", "/apix/Auth/signIn", "/apix/Auth/signInWithOAuth", "/apix/Auth/signUpWithInvitationCode", "/apix/Auth/signInWithOAuthAndInvitationCode").permitAll()
                .requestMatchers("/apix/Auth/refreshToken", "/apix/Auth/requestPasswordReset").permitAll()
                .requestMatchers("/apix/InvitationCode/verify").permitAll()
                .requestMatchers("/apix/FoodRawMaterial/**").permitAll()
                .requestMatchers("/apix/FoodRawMaterialSupplier/**").permitAll()
                .requestMatchers("/apix/FoodPreProduct/**").permitAll()
                .requestMatchers("/apix/FoodPreProductDetail/**").permitAll()
                .requestMatchers("/apix/MasterSupplier/**").permitAll()
                .requestMatchers("/apix/MasterMaker/**").permitAll()
                .requestMatchers("/apix/MasterSeller/**").permitAll()
                .requestMatchers("/apix/AllergenicControl/**").permitAll()
                .requestMatchers("/apix/MasterClassCategory/**").permitAll()
                .requestMatchers("/apix/**").authenticated()
                .requestMatchers("/error", "/actuator/**").permitAll()
                .anyRequest().denyAll())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
