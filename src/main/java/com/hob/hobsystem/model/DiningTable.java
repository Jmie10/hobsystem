package com.hob.hobsystem.model;

/** One table in the restaurant. The tables live in an array, not in the database. ENCAPSULATION: private fields. */
public class DiningTable {
    private final int no;
    private final int seats;
    private final String status;   // Available or Reserved

    public DiningTable(int no, int seats, String status) {
        this.no = no; this.seats = seats; this.status = status;
    }

    public int getNo() { return no; }
    public int getSeats() { return seats; }
    public String getStatus() { return status; }
}
