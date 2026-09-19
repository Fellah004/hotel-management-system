package com.hms.notificationservice.service.impl;

import com.hms.notificationservice.dto.response.NotificationResponse;
import com.hms.notificationservice.entity.Notification;
import com.hms.notificationservice.exception.ResourceNotFoundException;
import com.hms.notificationservice.repository.NotificationRepository;
import com.hms.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(String recipient, String role, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByRecipientOrRecipientRoleOrderByCreatedAtDesc(recipient, role, pageable);
        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        notification.setStatus("READ");
        notification = notificationRepository.save(notification);
        log.info("Marked notification #{} as READ", id);
        return mapToResponse(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipient(notification.getRecipient())
                .recipientRole(notification.getRecipientRole())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .channel(notification.getChannel())
                .status(notification.getStatus())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
