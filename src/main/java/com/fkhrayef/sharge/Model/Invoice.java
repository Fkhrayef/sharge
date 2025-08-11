package com.fkhrayef.sharge.Model;

import jakarta.persistence.*;
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
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "payment_method IN ('CASH', 'CARD')")
@Check(constraints = "kwh_delivered >= 0")
@Check(constraints = "total_cost >= 0")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Booking is required")
    @Column(columnDefinition = "INT NOT NULL")
    private Integer bookingId;

    @NotNull(message = "PaymentMethod is required")
    @Pattern(regexp = "^(CASH|CARD)$", message = "PaymentMethod is not valid")
    @Column(name = "payment_method", columnDefinition = "TEXT NOT NULL")
    private String paymentMethod;

    @NotNull(message = "KwhDelivered is required")
    @PositiveOrZero(message = "KwhDelivered must be greater than or equal to zero")
    @Column(name = "kwh_delivered", columnDefinition = "DOUBLE NOT NULL")
    private Double kwhDelivered;
    @NotNull(message = "TotalCost is required")
    @PositiveOrZero(message = "TotalCost must be greater than or equal to zero")
    @Column(name = "total_cost", columnDefinition = "DOUBLE NOT NULL")
    private Double totalCost;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isPaid;
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt; // Issued At
    @UpdateTimestamp
    private Instant updatedAt;
}
