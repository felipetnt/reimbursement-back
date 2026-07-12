package com.acme.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    protected UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
