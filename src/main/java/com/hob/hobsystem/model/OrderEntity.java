package com.hob.hobsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An order placed by a registered customer.
 * INHERITANCE: extends BaseEntity. POLYMORPHISM: implements Billable (total = sum of its lines).
 * ENCAPSULATION: private fields, and the item list can only change through addLine().
 */
@Entity
@Table(name = "orders")
public class OrderEntity extends BaseEntity implements Billable {
    private String customerName;
    private Long customerId;
    private String customerNo;          // ID of the customer who ordered, shown to staff
    private int tableNo;               // 0 for takeout
    private String orderType = "Dine-in";   // Dine-in or Takeout
    @Column(length = 300)
    private String notes;               // special instructions for the kitchen
    private String status = "Pending";  // Pending, Preparing, Ready, Served
    private boolean paid = false;
    private LocalDateTime createdAt;    // when the order was placed
    private LocalDateTime paidAt;       // when the payment was recorded

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderLine> items = new ArrayList<>();

    protected OrderEntity() {}   // needed by JPA

    public OrderEntity(String customerName, Long customerId, String customerNo, int tableNo) {
        this.customerName = customerName;
        this.customerId = customerId;
        this.customerNo = customerNo;
        this.tableNo = tableNo;
        this.createdAt = LocalDateTime.now().withNano(0);
    }

    public void setOrderType(String orderType) { this.orderType = orderType; }
    public void setNotes(String notes) { this.notes = notes; }

    public void addLine(OrderLine line) { items.add(line); }

    @Override
    public int total() {
        int sum = 0;
        for (Billable line : items) sum += line.total();   // each line works out its own total
        return sum;
    }

    public String getCustomerName() { return customerName; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerNo() { return customerNo; }
    public int getTableNo() { return tableNo; }
    public String getOrderType() { return orderType == null ? "Dine-in" : orderType; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) {
        this.paid = paid;
        if (paid && paidAt == null) paidAt = LocalDateTime.now().withNano(0);
    }
    public String getCreatedAt() { return createdAt == null ? null : createdAt.toString(); }
    public String getPaidAt() { return paidAt == null ? null : paidAt.toString(); }
    public List<OrderLine> getItems() { return Collections.unmodifiableList(items); }
}
