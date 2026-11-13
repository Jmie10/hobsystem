package com.hob.hobsystem.service;

import com.hob.hobsystem.model.AuthUser;
import com.hob.hobsystem.model.OrderEntity;
import com.hob.hobsystem.model.OrderLine;
import com.hob.hobsystem.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository repo;

    /** QUEUE (private = ENCAPSULATION): Pending orders wait here, first come first served. Rebuilt from MySQL on startup. */
    private final Deque<Long> kitchenQueue = new ArrayDeque<>();

    public OrderService(OrderRepository repo) {
        this.repo = repo;
        for (OrderEntity o : repo.findByStatusOrderByIdAsc("Pending")) kitchenQueue.addLast(o.getId());
    }

    public List<OrderEntity> all() { return repo.findAllByOrderByIdAsc(); }

    public List<OrderEntity> forCustomer(Long customerId) {
        return repo.findAllByOrderByIdAsc().stream().filter(o -> customerId.equals(o.getCustomerId())).toList();
    }

    public synchronized OrderEntity place(AuthUser u, String orderType, int tableNo, String notes, List<OrderLine> items) {
        boolean takeout = "Takeout".equalsIgnoreCase(orderType);
        if (takeout) tableNo = 0;           // takeout orders have no table
        else if (tableNo < 1 || tableNo > ReservationService.TABLE_COUNT)
            throw bad("Choose a valid table.");
        if (items == null || items.isEmpty())
            throw bad("Add at least one item to the order.");
        OrderEntity o = new OrderEntity(u.getName(), u.getId(), u.getCustomerNo(), tableNo);   // tied to a registered customer
        o.setOrderType(takeout ? "Takeout" : "Dine-in");
        if (notes != null && !notes.isBlank()) o.setNotes(notes.trim().length() > 300 ? notes.trim().substring(0, 300) : notes.trim());
        for (OrderLine l : items) {
            if (l.getName() == null || l.getQty() < 1 || l.getPrice() < 0) throw bad("Invalid item in the order.");
            o.addLine(l);
        }
        o = repo.save(o);
        kitchenQueue.addLast(o.getId());     // enqueue
        return o;
    }

    /** Dequeues the front order and starts preparing it. */
    public synchronized OrderEntity startNext() {
        Long id = kitchenQueue.pollFirst();  // dequeue
        if (id == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "The queue is empty.");
        OrderEntity o = get(id);
        o.setStatus("Preparing");
        return repo.save(o);
    }

    public synchronized OrderEntity advance(Long id) {
        OrderEntity o = get(id);
        switch (o.getStatus()) {
            case "Preparing" -> o.setStatus("Ready");
            case "Ready" -> o.setStatus("Served");
            default -> throw bad("Pending orders leave the queue with Start next order.");
        }
        return repo.save(o);
    }

    public synchronized OrderEntity pay(Long id) {
        OrderEntity o = get(id);
        o.setPaid(true);
        return repo.save(o);
    }

    /** A customer cancels their own order. Only possible while it is still waiting (Pending). */
    public synchronized void cancelOwn(AuthUser u, Long id) {
        OrderEntity o = get(id);
        if (!u.getId().equals(o.getCustomerId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "That is not your order.");
        if (!"Pending".equals(o.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Too late to cancel: the kitchen already started this order.");
        kitchenQueue.remove(id);
        repo.delete(o);
    }

    /** Used by Undo: removes an order that is still waiting in the queue. */
    public synchronized void undoPlace(Long id) {
        OrderEntity o = repo.findById(id).orElse(null);
        if (o == null) return;
        if (!"Pending".equals(o.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Too late to undo: the kitchen already started this order.");
        kitchenQueue.remove(id);
        repo.delete(o);
    }

    public synchronized int queueSize() { return kitchenQueue.size(); }

    private OrderEntity get(Long id) {
        return repo.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found."));
    }

    private ResponseStatusException bad(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}
