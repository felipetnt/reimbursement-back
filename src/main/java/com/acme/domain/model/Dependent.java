package com.acme.domain.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dependents")
public class Dependent extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @OneToMany(mappedBy = "dependent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TherapyType> therapyTypes = new ArrayList<>();

    @OneToMany(mappedBy = "dependent")
    private List<Reimbursement> reimbursements = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        this.family = family;
    }

    public List<TherapyType> getTherapyTypes() {
        return therapyTypes;
    }

    public void setTherapyTypes(List<TherapyType> therapyTypes) {
        this.therapyTypes = therapyTypes;
    }

    public List<Reimbursement> getReimbursements() {
        return reimbursements;
    }

    public void setReimbursements(List<Reimbursement> reimbursements) {
        this.reimbursements = reimbursements;
    }

    public void addTherapyType(TherapyType therapyType) {
        therapyTypes.add(therapyType);
        therapyType.setDependent(this);
    }

    public void removeTherapyType(TherapyType therapyType) {
        therapyTypes.remove(therapyType);
        therapyType.setDependent(null);
    }

    public void addReimbursement(Reimbursement reimbursement) {
        reimbursements.add(reimbursement);
        reimbursement.setDependent(this);
    }

    public void removeReimbursement(Reimbursement reimbursement) {
        reimbursements.remove(reimbursement);
        reimbursement.setDependent(null);
    }
}