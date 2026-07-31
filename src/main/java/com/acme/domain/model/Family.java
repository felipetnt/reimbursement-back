package com.acme.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "families")
public class Family extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "family")
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "family")
    private List<Dependent> dependents = new ArrayList<>();

    @OneToMany(mappedBy = "family")
    private List<Specialty> specialties = new ArrayList<>();

    @OneToMany(mappedBy = "family")
    private List<Professional> professionals = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Dependent> getDependents() {
        return dependents;
    }

    public List<Specialty> getSpecialties() {
        return specialties;
    }

    public List<Professional> getProfessionals() {
        return professionals;
    }

    public void addUser(User user) {
        users.add(user);
        user.setFamily(this);
    }

    public void removeUser(User user) {
        users.remove(user);
        user.setFamily(null);
    }

    public void addDependent(Dependent dependent) {
        dependents.add(dependent);
        dependent.setFamily(this);
    }

    public void removeDependent(Dependent dependent) {
        dependents.remove(dependent);
        dependent.setFamily(null);
    }

    public void addSpecialty(Specialty specialty) {
        specialties.add(specialty);
        specialty.setFamily(this);
    }

    public void removeSpecialty(Specialty specialty) {
        specialties.remove(specialty);
        specialty.setFamily(null);
    }

    public void addProfessional(Professional professional) {
        professionals.add(professional);
        professional.setFamily(this);
    }

    public void removeProfessional(Professional professional) {
        professionals.remove(professional);
        professional.setFamily(null);
    }
}