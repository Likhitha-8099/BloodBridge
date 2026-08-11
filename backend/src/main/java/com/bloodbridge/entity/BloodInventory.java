package com.bloodbridge.entity;

import com.bloodbridge.enums.BloodGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing hospital blood bank inventory for a specific blood group.
 */
@Entity
@Table(name = "blood_inventories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"hospital_id", "blood_group"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    @NotNull(message = "Hospital reference is required")
    private Hospital hospital;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @Builder.Default
    @Column(name = "available_units", nullable = false)
    private Integer availableUnits = 0;

    @Builder.Default
    @Column(name = "reserved_units", nullable = false)
    private Integer reservedUnits = 0;

    @Builder.Default
    @Column(name = "critical_threshold", nullable = false)
    private Integer criticalThreshold = 5;

    @Builder.Default
    @Column(name = "inventory_status", nullable = false)
    private String inventoryStatus = "NORMAL";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void calculateInventoryStatus() {
        if (availableUnits == null || availableUnits <= 0) {
            this.inventoryStatus = "OUT_OF_STOCK";
        } else if (availableUnits <= criticalThreshold / 2) {
            this.inventoryStatus = "CRITICAL";
        } else if (availableUnits <= criticalThreshold) {
            this.inventoryStatus = "LOW";
        } else {
            this.inventoryStatus = "NORMAL";
        }
    }
}
