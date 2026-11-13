package com.hob.hobsystem.controller;

import com.hob.hobsystem.model.AuthUser;
import com.hob.hobsystem.service.OrderService;
import com.hob.hobsystem.service.ReservationService;
import com.hob.hobsystem.service.UndoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UndoController {
    private final UndoService undo;
    private final OrderService orders;
    private final ReservationService reservations;

    public UndoController(UndoService undo, OrderService orders, ReservationService reservations) {
        this.undo = undo; this.orders = orders; this.reservations = reservations;
    }

    @PostMapping("/undo")
    public Map<String, String> undo(@RequestAttribute("auth") AuthUser user) {
        UndoService.UndoAction a = undo.pop(user.getCustomerNo());   // pop the last action off this customer's stack
        if (a == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Nothing to undo.");
        if (a.type().equals("ORDER")) orders.undoPlace(a.id());
        else reservations.cancel(a.id());
        return Map.of("undone", a.type());
    }
}
