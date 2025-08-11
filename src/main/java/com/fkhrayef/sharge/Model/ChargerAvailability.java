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
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')")
@Check(constraints = "start_time <= end_time")
public class ChargerAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Charger is required")
    @Column(columnDefinition = "INT NOT NULL")
    private Integer chargerId;

    @NotNull(message = "DayOfWeek is required")
    @Pattern(regexp = "^(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)$", message = "DayOfWeek is not valid")
    @Column(name = "day_of_week", columnDefinition = "TEXT NOT NULL")
    private String dayOfWeek;

    @NotNull(message = "StartTime is required")
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull(message = "EndTime is required")
    @Column(nullable = false)
    private LocalTime endTime;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
