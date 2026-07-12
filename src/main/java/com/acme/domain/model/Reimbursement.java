package com.acme.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reimbursements")
public class Reimbursement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_id", nullable = false)
    private Dependent dependent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapy_type_id", nullable = false)
    private TherapyType therapyType;

    @Column(name = "reference_month", nullable = false)
    private LocalDate referenceMonth;

    @Column(name = "sessions_quantity", nullable = false)
    private Integer sessionsQuantity;

    @Column(name = "session_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal sessionValue;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "therapist_name", nullable = false, length = 100)
    private String therapistName;

    @Column(name = "therapist_pix", nullable = false, length = 150)
    private String therapistPix;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "reimbursement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Solicitation> solicitations = new ArrayList<>();

    public void recalculateTotalAmount() {
        if (sessionsQuantity != null && sessionValue != null) {
            this.totalAmount = sessionValue
                    .multiply(BigDecimal.valueOf(sessionsQuantity))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    public Dependent getDependent() {
        return dependent;
    }

    public void setDependent(Dependent dependent) {
        this.dependent = dependent;
    }

    public TherapyType getTherapyType() {
        return therapyType;
    }

    public void setTherapyType(TherapyType therapyType) {
        this.therapyType = therapyType;
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
    }

    public BigDecimal getSessionValue() {
        return sessionValue;
    }

    public void setSessionValue(BigDecimal sessionValue) {
        this.sessionValue = sessionValue;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getTherapistName() {
        return therapistName;
    }

    public void setTherapistName(String therapistName) {
        this.therapistName = therapistName;
    }

    public String getTherapistPix() {
        return therapistPix;
    }

    public void setTherapistPix(String therapistPix) {
        this.therapistPix = therapistPix;
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

    public void setSolicitations(List<Solicitation> solicitations) {
        this.solicitations = solicitations;
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