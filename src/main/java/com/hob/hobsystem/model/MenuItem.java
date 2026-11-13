package com.hob.hobsystem.model;

import jakarta.persistence.Entity;

/** One row of the menu table. INHERITANCE: extends BaseEntity. ENCAPSULATION: private fields with checked setters. */
@Entity
public class MenuItem extends BaseEntity {
    private String name;
    private String category;
    private int price;
    private boolean available = true;

    protected MenuItem() {}   // needed by JPA

    public MenuItem(String name, String category, int price) {
        setName(name);
        setCategory(category);
        setPrice(price);
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        this.name = name.trim();
    }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        this.category = (category == null || category.isBlank()) ? "Meals" : category.trim();
    }

    public int getPrice() { return price; }
    public void setPrice(int price) {
        if (price <= 0) throw new IllegalArgumentException("Price must be above zero.");
        this.price = price;
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
