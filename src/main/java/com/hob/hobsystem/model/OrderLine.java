package com.hob.hobsystem.model;

import jakarta.persistence.Embeddable;

/** One line of an order (dish, price, quantity). POLYMORPHISM: implements Billable with its own total(). */
@Embeddable
public class OrderLine implements Billable {
    private String name;
    private int price;
    private int qty;

    public OrderLine() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    @Override
    public int total() { return price * qty; }      // price times quantity
}
