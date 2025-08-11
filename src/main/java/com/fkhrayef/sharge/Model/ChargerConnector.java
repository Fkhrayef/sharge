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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "connector_type IN ('TYPE1', 'TYPE2', 'CCS1', 'CCS2', 'CHADEMO', 'TESLA')")
public class ChargerConnector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Charger is required")
    @Column(columnDefinition = "INT NOT NULL")
    private Integer chargerId;

    @NotNull(message = "ConnectorType is required")
    @Pattern(regexp = "^(TYPE1|TYPE2|CCS1|CCS2|CHADEMO|TESLA)$", message = "ConnectorType is not valid")
    @Column(name = "connector_type", columnDefinition = "TEXT NOT NULL")
    private String connectorType;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
