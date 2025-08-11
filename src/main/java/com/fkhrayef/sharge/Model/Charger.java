package com.fkhrayef.sharge.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "price_per_kwh >= 0")
@Check(constraints = "max_kw >= 0")
public class Charger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Owner is required")
    @Column(columnDefinition = "INT NOT NULL")
    private Integer ownerId; // Change to userId

    @NotEmpty(message = "Name is required")
    @Column(columnDefinition = "TEXT NOT NULL")
    private String name;
    @NotEmpty(message = "Description is required")
    @Column(columnDefinition = "TEXT NOT NULL")
    private String description;

    @Column(columnDefinition = "DOUBLE")
    private Double latitude;
    @Column(columnDefinition = "DOUBLE")
    private Double longitude;
    @Column(columnDefinition = "TEXT")
    private String address;
    @NotEmpty(message = "NationalAddress is required")
    @Pattern(regexp = "^[A-Z]{4}\\d{4}$", message = "Invalid Saudi National Address format")
    @Column(columnDefinition = "TEXT NOT NULL")
    private String nationalAddress;

    @NotNull(message = "Price per kwh is required")
    @PositiveOrZero(message = "Price per kwh must be greater than or equal to zero")
    @Column(name = "price_per_kwh", columnDefinition = "DOUBLE NOT NULL")
    private Double pricePerKwh;
    @NotNull(message = "Max kw is required")
    @PositiveOrZero(message = "Max kw must be greater than or equal to zero")
    @Column(name = "max_kw", columnDefinition = "DOUBLE NOT NULL")
    private Double maxKw;

    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
