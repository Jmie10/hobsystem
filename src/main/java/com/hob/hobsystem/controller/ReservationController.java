package com.hob.hobsystem.controller;

import com.hob.hobsystem.model.AuthUser;
import com.hob.hobsystem.model.DiningTable;
import com.hob.hobsystem.model.Reservation;
import com.hob.hobsystem.model.WaitlistEntry;
import com.hob.hobsystem.service.ReservationService;
import com.hob.hobsystem.service.UndoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReservationController {
    private final ReservationService res;
    private final UndoService undo;
    public ReservationController(ReservationService res, UndoService undo) { this.res = res; this.undo = undo; }

    @GetMapping("/tables")
    public DiningTable[] tables(@RequestParam("date") String date, @RequestParam("time") String time) {
        return res.tables(date, time);
    }

    @GetMapping("/reservations")
    public List<Reservation> all(@RequestAttribute("auth") AuthUser user) {
        return user.seesOnlyOwnRecords() ? res.forCustomer(user.getId()) : res.all();
    }

    @PostMapping("/reservations")
    public Reservation reserve(@RequestBody Requests.ReserveReq r, @RequestAttribute("auth") AuthUser user) {
        Reservation saved = res.reserve(user, r.tableNo(), r.date(), r.time(), r.guests());
        undo.push(user.getCustomerNo(), "RESERVATION", saved.getId());
        return saved;
    }

    @PostMapping("/reservations/{id}/confirm")
    public Reservation confirm(@PathVariable("id") Long id) { return res.confirm(id); }

    @PostMapping("/reservations/{id}/cancel")
    public Reservation cancel(@PathVariable("id") Long id) { return res.cancelPending(id); }

    @PostMapping("/my/reservations/{id}/cancel")
    public Reservation cancelMine(@PathVariable("id") Long id, @RequestAttribute("auth") AuthUser user) { return res.cancelOwn(user, id); }

    @PostMapping("/reservations/{id}/complete")
    public Reservation complete(@PathVariable("id") Long id) { return res.complete(id); }

    @GetMapping("/waitlist")
    public List<WaitlistEntry> waitlist(@RequestAttribute("auth") AuthUser user) {
        return user.seesOnlyOwnRecords() ? res.waitlistFor(user.getId()) : res.waitlist();
    }

    @PostMapping("/waitlist")
    public WaitlistEntry join(@RequestBody Requests.WaitReq r, @RequestAttribute("auth") AuthUser user) {
        return res.join(user, r.date(), r.time(), r.guests());
    }
}
