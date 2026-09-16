package com.hms.authservice;

import com.hms.authservice.entity.Role;
import com.hms.authservice.entity.User;
import com.hms.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(User.builder()
                        .username("admin")
                        .email("admin@hms.com")
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(Role.ADMIN)
                        .active(true)
                        .build());

                userRepository.save(User.builder()
                        .username("owner")
                        .email("owner@hms.com")
                        .password(passwordEncoder.encode("Owner@123"))
                        .role(Role.OWNER)
                        .active(true)
                        .build());

                userRepository.save(User.builder()
                        .username("manager")
                        .email("manager@hms.com")
                        .password(passwordEncoder.encode("Manager@123"))
                        .role(Role.MANAGER)
                        .active(true)
                        .build());

                userRepository.save(User.builder()
                        .username("receptionist")
                        .email("receptionist@hms.com")
                        .password(passwordEncoder.encode("Reception@123"))
                        .role(Role.RECEPTIONIST)
                        .active(true)
                        .build());

                userRepository.save(User.builder()
                        .username("housekeeper")
                        .email("housekeeper@hms.com")
                        .password(passwordEncoder.encode("Housekeeper@123"))
                        .role(Role.HOUSEKEEPER)
                        .active(true)
                        .build());

                userRepository.save(User.builder()
                        .username("guest")
                        .email("guest@hms.com")
                        .password(passwordEncoder.encode("Guest@123"))
                        .role(Role.GUEST)
                        .active(true)
                        .build());
            }
        };
    }
}
