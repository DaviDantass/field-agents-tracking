package com.davidantasdev.fieldagentstracking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sync_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime syncStartTime;

    @Column(nullable = false)
    private LocalDateTime syncEndTime;

    @Column(nullable = false)
    private Integer totalLocationsReceived;

    @Column(nullable = false)
    private Integer locationsProcessed;

    @Column(nullable = false)
    private Integer locationsSkipped;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private String status; // SUCCESS, PARTIAL_SUCCESS, FAILED

    @Column
    private String syncToken;

    @PrePersist
    void prePersist() {
        if (syncStartTime == null) {
            syncStartTime = LocalDateTime.now();
        }
        if (syncEndTime == null) {
            syncEndTime = LocalDateTime.now();
        }
    }
}
