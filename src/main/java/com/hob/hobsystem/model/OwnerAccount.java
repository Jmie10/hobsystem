package com.hob.hobsystem.model;

/** INHERITANCE: the owner is a StaffAccount that can also manage the menu and see reports and customers. */
public class OwnerAccount extends StaffAccount {
    public OwnerAccount(Long id, String customerNo, String name) { super(id, customerNo, name); }

    @Override public String getRole() { return "ADMIN"; }

    @Override
    public boolean canAccess(String method, String path) {
        return super.canAccess(method, path) || ownerOnly(method, path);   // everything staff can do, plus owner-only
    }
}
