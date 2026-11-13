package com.hob.hobsystem.model;

/**
 * ABSTRACTION: an interface that says "this thing has a total in pesos" without saying how it is computed.
 * POLYMORPHISM: OrderLine and OrderEntity both implement total(), but each computes it differently.
 */
public interface Billable {
    int total();
}
