package com.hms.notification.controller;

import com.hms.notification.entity.Notification;
import com.hms.notification.entity.NotificationTemplate;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @PostMapping
    public ResponseEntity<Notification> sendNotification(
            @Valid @RequestBody Notification notification) {

        return ResponseEntity.ok(notification);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications() {
        return ResponseEntity.ok(List.of());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                "Notification " + id + " status changed to " + status
        );
    }

    @PostMapping("/templates")
    public ResponseEntity<NotificationTemplate> createTemplate(
            @Valid @RequestBody NotificationTemplate template) {

        return ResponseEntity.ok(template);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<NotificationTemplate>> getTemplates() {
        return ResponseEntity.ok(List.of());
    }
}
