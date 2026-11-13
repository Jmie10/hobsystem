package com.hob.hobsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * A user account row. INHERITANCE: extends BaseEntity.
 * ENCAPSULATION: private fields; the password hash is never sent to the web page.
 */
@Entity
public class Customer extends BaseEntity {
    private String customerNo;          // automatic ID shown to staff, for example 009
    private String name;                // full name, built from the three parts below
    private String firstName;
    private String middleName;
    private String lastName;
    @Column(unique = true)
    private String email;               // lower case, used to log in and to reset the password
    @Column(unique = true)
    private String contactNumber;
    private String passwordHash;        // salt:hash, never the real password
    private String userRole = "CUSTOMER";   // CUSTOMER, STAFF or ADMIN (owner)

    protected Customer() {}   // needed by JPA

    public Customer(String name, String contactNumber, String passwordHash, String userRole) {
        this.name = name; this.contactNumber = contactNumber;
        this.passwordHash = passwordHash; this.userRole = userRole;
    }

    public String getCustomerNo() { return customerNo; }
    public void setCustomerNo(String customerNo) { this.customerNo = customerNo; }
    public String getName() { return name; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public void setNames(String first, String middle, String last) { this.firstName = first; this.middleName = middle; this.lastName = last; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContactNumber() { return contactNumber; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getUserRole() { return userRole; }
}
