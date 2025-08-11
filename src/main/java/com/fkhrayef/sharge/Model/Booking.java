package com.fkhrayef.sharge.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "status IN ('RESERVED', 'CANCELLED', 'IN_PROGRESS', 'COMPLETED')")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "User is required")
    @Column(columnDefinition = "INT NOT NULL")
    private Integer userId; // Remove? we can reach it from carId (but faster this way)
    @NotNull(message = "Car is required")
    @Column(columnDefinition = "INT NOT NULL")
    private Integer carId;
    @NotNull(message = "Charger is required")
    @Column(columnDefinition = "INT NOT NULL")
    private Integer chargerId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Pattern(regexp = "^(RESERVED|CANCELLED|IN_PROGRESS|COMPLETED)$", message = "Status is not valid")
    @Column(name = "status", columnDefinition = "TEXT DEFAULT 'RESERVED'")
    private String status;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt; // Booked At
    @UpdateTimestamp
    private Instant updatedAt;
}
