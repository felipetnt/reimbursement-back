package com.acme.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "specialties",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_specialty_family_name",
                        columnNames = {"family_id", "name"}
                )
        }
)
public class Specialty extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @OneToMany(mappedBy = "specialty")
    private List<Professional> professionals = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        this.family = family;
    }

    public List<Professional> getProfessionals() {
        return professionals;
    }

    public void addProfessional(Professional professional) {
        professionals.add(professional);
        professional.setSpecialty(this);
    }

    public void removeProfessional(Professional professional) {
        professionals.remove(professional);
        professional.setSpecialty(null);
    }
}