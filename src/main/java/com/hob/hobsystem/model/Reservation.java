package com.hob.hobsystem.model;

import jakarta.persistence.Entity;

/** INHERITANCE: extends BaseEntity. ENCAPSULATION: private fields, changed only through methods. */
@Entity
public class Reservation extends BaseEntity {
    private String name;
    private int tableNo;
    private String bookingDate;   // yyyy-MM-dd
    private String bookingTime;   // HH:mm
    private int guests;
    private String status = "Pending";   // Pending, Confirmed, Cancelled
    private Long customerId;
    private String customerNo;

    protected Reservation() {}   // needed by JPA

    public Reservation(String name, int tableNo, String date, String time, int guests) {
        this.name = name; this.tableNo = tableNo; this.bookingDate = date;
        this.bookingTime = time; this.guests = guests;
    }

    public void assignCustomer(Long id, String customerNo) { this.customerId = id; this.customerNo = customerNo; }

    public String getName() { return name; }
    public int getTableNo() { return tableNo; }
    public String getBookingDate() { return bookingDate; }
    public String getBookingTime() { return bookingTime; }
    public int getGuests() { return guests; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerNo() { return customerNo; }
}
