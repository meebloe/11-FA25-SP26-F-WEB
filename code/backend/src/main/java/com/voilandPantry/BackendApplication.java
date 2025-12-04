package com.voilandPantry;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.voilandPantry.models.AdminUser;
import com.voilandPantry.repositories.AdminUserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner createDefaultAdmin(AdminUserRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                String username = "admin";
                String password = "password123";

                // hash password
                String hash = new BCryptPasswordEncoder().encode(password);

                AdminUser admin = new AdminUser(username, hash);
                repo.save(admin);

                System.out.println("Default admin user created (username: admin, password: password123)");
            }
        };
    }
}