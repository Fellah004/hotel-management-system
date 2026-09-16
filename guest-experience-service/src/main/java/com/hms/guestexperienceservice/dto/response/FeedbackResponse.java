package com.hms.guestexperienceservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {
    private Long id;
    private Long guestId;
    private Long reservationId;
    private Long roomId;
    private Integer rating;
    private String comments;
    private LocalDateTime createdAt;
}
