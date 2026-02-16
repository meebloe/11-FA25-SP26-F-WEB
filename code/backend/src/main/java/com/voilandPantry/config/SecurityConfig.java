package com.voilandPantry.config;

import com.voilandPantry.repositories.AdminUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {

    private final AdminUserRepository adminUserRepository;

    public SecurityConfig(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/login", "/register_student", "/register", "/welcome", "/css/**", "/images/**").permitAll()
                .requestMatchers("/admin/**", "/inventory", "/card-reader", "/volunteer-form", "/report", "/report/download").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/admin-login")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
            )

            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .invalidSessionUrl("/admin-login")
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )

            .csrf().disable();

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AdminUserRepository repo) {
        return username -> repo.findByUsername(username)
                .map(admin -> User.withUsername(admin.getUsername())
                        .password(admin.getPasswordHash())
                        .roles("ADMIN")
                        .build()
                )
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}