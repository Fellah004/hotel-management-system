package com.hms.authservice.service;

import com.hms.authservice.entity.User;
import com.hms.authservice.event.publisher.SecurityEventPublisher;
import com.hms.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final SecurityEventPublisher securityEventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedAttempt(Long userId, int maxAttempts) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return 0;

        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxAttempts) {
            user.setLocked(true);
            user.setLockTime(LocalDateTime.now());
            userRepository.save(user);
            securityEventPublisher.publishAccountLocked(user.getId(), user.getUsername(), user.getEmail(), "Max failed login attempts exceeded");
            log.warn("Account locked for user: {} after {} failed attempts", user.getUsername(), attempts);
        } else {
            userRepository.save(user);
            log.warn("User {} failed login attempt {} of {}", user.getUsername(), attempts, maxAttempts);
        }
        return attempts;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0 || user.isLocked() || user.getLockTime() != null) {
                user.setFailedLoginAttempts(0);
                user.setLocked(false);
                user.setLockTime(null);
                userRepository.save(user);
                log.info("Reset login attempts and lock state for user: {}", user.getUsername());
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void unlockAccount(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLocked(false);
            user.setFailedLoginAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
            log.info("Account automatically unlocked after 15 minutes for user: {}", user.getUsername());
        });
    }
}
