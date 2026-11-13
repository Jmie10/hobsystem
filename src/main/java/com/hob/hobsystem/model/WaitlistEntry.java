package com.hob.hobsystem.model;

import jakarta.persistence.Entity;

/** INHERITANCE: extends BaseEntity. ENCAPSULATION: private fields. */
@Entity
public class WaitlistEntry extends BaseEntity {
    private String name;
    private String bookingDate;
    private String bookingTime;
    private int guests;
    private Long customerId;
    private String customerNo;

    protected WaitlistEntry() {}   // needed by JPA

    public WaitlistEntry(String name, String date, String time, int guests) {
        this.name = name; this.bookingDate = date; this.bookingTime = time; this.guests = guests;
    }

    public void assignCustomer(Long id, String customerNo) { this.customerId = id; this.customerNo = customerNo; }

    public String getName() { return name; }
    public String getBookingDate() { return bookingDate; }
    public String getBookingTime() { return bookingTime; }
    public int getGuests() { return guests; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerNo() { return customerNo; }
}
