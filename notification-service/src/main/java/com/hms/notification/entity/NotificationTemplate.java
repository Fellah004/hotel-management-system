package com.hms.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Template name is required")
    private String name;

    @NotBlank(message = "Event type is required")
    private String eventType;

    @NotBlank(message = "Channel is required")
    private String channel;

    @NotBlank(message = "Template message is required")
    @Size(max = 1000, message = "Template message must not exceed 1000 characters")
    private String message;

    public NotificationTemplate() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
