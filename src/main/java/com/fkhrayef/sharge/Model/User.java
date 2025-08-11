package com.fkhrayef.sharge.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Name is required")
    @Column(columnDefinition = "TEXT NOT NULL")
    private String name;

    @NotEmpty(message = "Email is required")
    @Email(message = "Email is not valid")
    @Column(columnDefinition = "TEXT NOT NULL UNIQUE")
    private String email;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer noShowCount; // now doesn't do anything

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
