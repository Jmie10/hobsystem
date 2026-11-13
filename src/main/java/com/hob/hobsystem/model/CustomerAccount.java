package com.hob.hobsystem.model;

/** INHERITANCE: a customer is an AuthUser. Can view the menu, order, reserve and undo. Sees only their own records. */
public class CustomerAccount extends AuthUser {
    public CustomerAccount(Long id, String customerNo, String name) { super(id, customerNo, name); }

    @Override public String getRole() { return "CUSTOMER"; }
    @Override public boolean seesOnlyOwnRecords() { return true; }

    @Override
    public boolean canAccess(String method, String path) {
        if (authPath(path)) return true;
        if (ownerOnly(method, path)) return false;
        return method.equals("GET") || customerAction(method, path);
    }
}
