package com.acme.domain.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "therapy_types")
public class TherapyType extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_id", nullable = false)
    private Dependent dependent;

    @OneToMany(mappedBy = "therapyType")
    private List<Reimbursement> reimbursements = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Dependent getDependent() {
        return dependent;
    }

    public void setDependent(Dependent dependent) {
        this.dependent = dependent;
    }

    public List<Reimbursement> getReimbursements() {
        return reimbursements;
    }

    public void setReimbursements(List<Reimbursement> reimbursements) {
        this.reimbursements = reimbursements;
    }

    public void addReimbursement(Reimbursement reimbursement) {
        reimbursements.add(reimbursement);
        reimbursement.setTherapyType(this);
    }

    public void removeReimbursement(Reimbursement reimbursement) {
        reimbursements.remove(reimbursement);
        reimbursement.setTherapyType(null);
    }
}