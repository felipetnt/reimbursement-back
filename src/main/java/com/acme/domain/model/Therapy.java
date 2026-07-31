package com.acme.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "therapies")
public class Therapy extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_id", nullable = false)
    private Dependent dependent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @Column(
            name = "default_session_value",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal defaultSessionValue;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "therapy")
    @OrderBy("referenceMonth DESC")
    private List<Reimbursement> reimbursements = new ArrayList<>();

    public Dependent getDependent() {
        return dependent;
    }

    public void setDependent(Dependent dependent) {
        this.dependent = dependent;
    }

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public BigDecimal getDefaultSessionValue() {
        return defaultSessionValue;
    }

    public void setDefaultSessionValue(BigDecimal defaultSessionValue) {
        this.defaultSessionValue = defaultSessionValue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Reimbursement> getReimbursements() {
        return reimbursements;
    }

    public void addReimbursement(Reimbursement reimbursement) {
        reimbursements.add(reimbursement);
        reimbursement.setTherapy(this);
    }

    public void removeReimbursement(Reimbursement reimbursement) {
        reimbursements.remove(reimbursement);
        reimbursement.setTherapy(null);
    }
}
