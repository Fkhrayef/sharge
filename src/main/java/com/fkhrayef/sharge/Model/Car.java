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
@Check(constraints = "connector_type IN ('TYPE1', 'TYPE2', 'CCS1', 'CCS2', 'CHADEMO', 'TESLA')")
@Check(constraints = "max_kw >= 0")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "User is required")
    @Column(columnDefinition = "INT NOT NULL")
    private Integer userId;

    @NotEmpty(message = "Make is required")
    @Column(columnDefinition = "TEXT NOT NULL")
    private String make;

    @NotEmpty(message = "Model is required")
    @Column(columnDefinition = "TEXT NOT NULL")
    private String model;

    @NotEmpty(message = "Nickname is required")
    @Column(columnDefinition = "TEXT NOT NULL")
    private String nickname;

    @NotNull(message = "ConnectorType is required")
    @Pattern(regexp = "^(TYPE1|TYPE2|CCS1|CCS2|CHADEMO|TESLA)$", message = "ConnectorType is not valid")
    @Column(name = "connector_type", columnDefinition = "TEXT NOT NULL")
    private String connectorType;

    @NotNull(message = "MaxKw is required")
    @PositiveOrZero(message = "MaxKw must be greater than or equal to zero")
    @Column(columnDefinition = "DOUBLE NOT NULL")
    private Double maxKw;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
