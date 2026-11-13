package com.hob.hobsystem.model;

import jakarta.persistence.*;

/**
 * INHERITANCE + ABSTRACTION: every database entity extends this abstract parent,
 * so the id column is written once and reused by all child classes.
 * ENCAPSULATION: the id is private; it can only be read through getId().
 */
@MappedSuperclass
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() { return id; }
}
