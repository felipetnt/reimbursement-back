package com.acme.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "reimbursements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reimbursement_therapy_month",
                        columnNames = {"therapy_id", "reference_month"}
                )
        }
)
public class Reimbursement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapy_id", nullable = false)
    private Therapy therapy;

    @Column(name = "reference_month", nullable = false)
    private LocalDate referenceMonth;

    @Column(name = "sessions_quantity", nullable = false)
    private Integer sessionsQuantity;

    @Column(
            name = "session_value",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal sessionValue;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "reimbursement")
    @OrderBy("attemptNumber ASC")
    private List<Solicitation> solicitations = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void recalculateTotalAmount() {
        if (sessionsQuantity == null || sessionValue == null) {
            return;
        }

        this.totalAmount = sessionValue
                .multiply(BigDecimal.valueOf(sessionsQuantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Therapy getTherapy() {
        return therapy;
    }

    public void setTherapy(Therapy therapy) {
        this.therapy = therapy;
    }

    public LocalDate getReferenceMonth() {
        return referenceMonth;
    }

    public void setReferenceMonth(LocalDate referenceMonth) {
        this.referenceMonth = referenceMonth;
    }

    public Integer getSessionsQuantity() {
        return sessionsQuantity;
    }

    public void setSessionsQuantity(Integer sessionsQuantity) {
        this.sessionsQuantity = sessionsQuantity;
        recalculateTotalAmount();
    }

    public BigDecimal getSessionValue() {
        return sessionValue;
    }

    public void setSessionValue(BigDecimal sessionValue) {
        this.sessionValue = sessionValue;
        recalculateTotalAmount();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Solicitation> getSolicitations() {
        return solicitations;
    }

    public void addSolicitation(Solicitation solicitation) {
        solicitations.add(solicitation);
        solicitation.setReimbursement(this);
    }

    public void removeSolicitation(Solicitation solicitation) {
        solicitations.remove(solicitation);
        solicitation.setReimbursement(null);
    }
}