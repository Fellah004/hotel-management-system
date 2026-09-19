package com.hms.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_recipient", columnList = "recipient"),
        @Index(name = "idx_notifications_role", columnList = "recipientRole"),
        @Index(name = "idx_notifications_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipient; // user email, username, or staff code

    private String recipientRole; // ADMIN, OWNER, MANAGER, RECEPTIONIST, HOUSEKEEPER, GUEST

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String channel = "IN_APP"; // EMAIL, SMS, IN_APP

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "UNREAD"; // UNREAD, READ

    private String referenceType; // RESERVATION, PAYMENT, TASK, COMPLAINT, INVENTORY, SUMMARY

    private Long referenceId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
