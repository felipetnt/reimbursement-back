package com.acme.domain.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "family")
public class Family extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dependent> dependents = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Dependent> getDependents() {
        return dependents;
    }

    public void setDependents(List<Dependent> dependents) {
        this.dependents = dependents;
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
}