package com.hms.notificationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String recipient;
    private String recipientRole;
    private String title;
    private String message;
    private String channel;
    private String status;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime createdAt;
}
