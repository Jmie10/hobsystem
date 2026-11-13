package com.hob.hobsystem.service;

import com.hob.hobsystem.model.OrderEntity;
import com.hob.hobsystem.model.OrderLine;
import com.hob.hobsystem.model.Reservation;
import com.hob.hobsystem.repository.OrderRepository;
import com.hob.hobsystem.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportService {
    private final OrderRepository orders;
    private final ReservationRepository reservations;
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final UndoService undoService;
    private final MenuService menuService;

    public ReportService(OrderRepository orders, ReservationRepository reservations, OrderService orderService,
                         ReservationService reservationService, UndoService undoService, MenuService menuService) {
        this.orders = orders; this.reservations = reservations; this.orderService = orderService;
        this.reservationService = reservationService; this.undoService = undoService; this.menuService = menuService;
    }

    public Map<String, Object> build() {
        List<OrderEntity> all = orders.findAll();
        int sales = 0;
        Map<String, Integer> counts = new HashMap<>();
        for (OrderEntity o : all) {
            if (o.isPaid()) sales += o.total();          // POLYMORPHISM: total() comes from Billable
            for (OrderLine l : o.getItems()) counts.merge(l.getName(), l.getQty(), Integer::sum);
        }
        List<Map<String, Object>> top = new ArrayList<>();
        counts.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .limit(5)
            .forEach(e -> {
                Map<String, Object> t = new LinkedHashMap<>();
                t.put("name", e.getKey());
                t.put("qty", e.getValue());
                top.add(t);
            });
        int activeRes = 0;
        for (Reservation r : reservations.findAll()) if (!"Cancelled".equals(r.getStatus())) activeRes++;

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sales", sales);
        m.put("orders", all.size());
        m.put("reservations", activeRes);
        m.put("top", top);
        m.put("undoStack", undoService.totalSize());
        m.put("kitchenQueue", orderService.queueSize());
        m.put("waitlist", reservationService.waitlistSize());
        m.put("bookingKeys", reservationService.bookingKeyCount());
        m.put("tables", ReservationService.TABLE_COUNT);
        m.put("categories", menuService.categoryCount());
        return m;
    }
}
