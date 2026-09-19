package com.hms.reportingservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_performance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffPerformanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long staffId;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer tasksCompleted = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer tasksRejected = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer shiftsCompleted = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer attendanceDays = 0;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
