package com.hms.notificationservice.repository;

import com.hms.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);
    List<Notification> findByRecipientRoleOrderByCreatedAtDesc(String recipientRole);
    Page<Notification> findByRecipientOrRecipientRoleOrderByCreatedAtDesc(String recipient, String recipientRole, Pageable pageable);
    long countByRecipientAndStatus(String recipient, String status);
}
