package com.hob.hobsystem.controller;

import com.hob.hobsystem.model.AuthUser;
import com.hob.hobsystem.model.OrderEntity;
import com.hob.hobsystem.service.OrderService;
import com.hob.hobsystem.service.UndoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderService orders;
    private final UndoService undo;
    public OrderController(OrderService orders, UndoService undo) { this.orders = orders; this.undo = undo; }

    /** Customers see only their own orders. Staff and the owner see all of them. */
    @GetMapping("/orders")
    public List<OrderEntity> all(@RequestAttribute("auth") AuthUser user) {
        return user.seesOnlyOwnRecords() ? orders.forCustomer(user.getId()) : orders.all();
    }

    @PostMapping("/orders")
    public OrderEntity place(@RequestBody Requests.OrderReq r, @RequestAttribute("auth") AuthUser user) {
        OrderEntity o = orders.place(user, r.orderType(), r.tableNo(), r.notes(), r.items());
        undo.push(user.getCustomerNo(), "ORDER", o.getId());        // push onto this customer's undo stack
        return o;
    }

    @PostMapping("/kitchen/start-next")
    public OrderEntity startNext() { return orders.startNext(); }

    @PostMapping("/orders/{id}/advance")
    public OrderEntity advance(@PathVariable("id") Long id) { return orders.advance(id); }

    @PostMapping("/orders/{id}/pay")
    public OrderEntity pay(@PathVariable("id") Long id) { return orders.pay(id); }
}
