package com.hob.hobsystem.model;

/** INHERITANCE: staff is an AuthUser. Runs the kitchen queue, reservations and billing. Sees all records. */
public class StaffAccount extends AuthUser {
    public StaffAccount(Long id, String customerNo, String name) { super(id, customerNo, name); }

    @Override public String getRole() { return "STAFF"; }
    @Override public boolean seesOnlyOwnRecords() { return false; }

    @Override
    public boolean canAccess(String method, String path) {
        if (authPath(path)) return true;
        return !ownerOnly(method, path) && !customerAction(method, path);
    }
}
