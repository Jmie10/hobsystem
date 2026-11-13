package com.hob.hobsystem.model;

/**
 * ABSTRACTION: the abstract parent of every logged-in user. It says WHAT a user can answer
 * (role, what it may access) but not HOW; each child class decides.
 * ENCAPSULATION: the fields are private and read through getters.
 */
public abstract class AuthUser {
    private final Long id;
    private final String customerNo;
    private final String name;

    protected AuthUser(Long id, String customerNo, String name) {
        this.id = id; this.customerNo = customerNo; this.name = name;
    }

    public Long getId() { return id; }
    public String getCustomerNo() { return customerNo; }
    public String getName() { return name; }

    /** POLYMORPHISM: every subclass answers these differently. */
    public abstract String getRole();
    public abstract boolean canAccess(String method, String path);
    public abstract boolean seesOnlyOwnRecords();

    // helpers shared by all child classes
    protected static boolean authPath(String path) {
        return path.equals("/api/auth/logout") || path.equals("/api/auth/me") || path.startsWith("/api/account");
    }
    protected static boolean ownerOnly(String method, String path) {
        return path.startsWith("/api/reports") || path.startsWith("/api/customers")
            || (path.startsWith("/api/menu") && !method.equals("GET"));
    }
    protected static boolean customerAction(String method, String path) {
        return !method.equals("GET") && (path.equals("/api/orders") || path.equals("/api/reservations")
            || path.equals("/api/waitlist") || path.equals("/api/undo"));
    }
}
