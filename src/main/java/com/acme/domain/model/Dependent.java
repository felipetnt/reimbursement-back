package com.acme.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dependents")
public class Dependent extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @OneToMany(mappedBy = "dependent")
    private List<Therapy> therapies = new ArrayList<>();

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

    public List<Therapy> getTherapies() {
        return therapies;
    }

    public void addTherapy(Therapy therapy) {
        therapies.add(therapy);
        therapy.setDependent(this);
    }

    public void removeTherapy(Therapy therapy) {
        therapies.remove(therapy);
        therapy.setDependent(null);
    }
}