package com.hob.hobsystem.service;

import com.hob.hobsystem.model.AuthUser;
import com.hob.hobsystem.model.DiningTable;
import com.hob.hobsystem.model.Reservation;
import com.hob.hobsystem.model.WaitlistEntry;
import com.hob.hobsystem.repository.ReservationRepository;
import com.hob.hobsystem.repository.WaitlistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class ReservationService {
    /** ARRAY: seats of each table (Table 1 = 2 seats, ... Table 8 = 8 seats). */
    public static final int[] SEATS = {2, 2, 4, 4, 4, 6, 6, 8};
    public static final int TABLE_COUNT = SEATS.length;
    /** House of Brew is open 6:00 PM to 3:00 AM. Times after midnight belong to the same night. */
    private static final Set<String> OPEN_SLOTS = Set.of("18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "00:00", "01:00", "02:00");

    private final ReservationRepository repo;
    private final WaitlistRepository waitRepo;

    /** HASHSET (private = ENCAPSULATION): "table|date|time" of active bookings, for instant double-booking checks. */
    private final Set<String> bookingKeys = new HashSet<>();
    /** HASHSET: "customerNo|date|time", so one customer cannot book twice at the same time. */
    private final Set<String> personKeys = new HashSet<>();
    /** QUEUE: customers waiting for a table, first come first served. */
    private final Deque<WaitlistEntry> waitlist = new ArrayDeque<>();

    public ReservationService(ReservationRepository repo, WaitlistRepository waitRepo) {
        this.repo = repo;
        this.waitRepo = waitRepo;
        for (Reservation r : repo.findAllByOrderByIdAsc()) if (!"Cancelled".equals(r.getStatus()) && !"Completed".equals(r.getStatus())) index(r);
        waitlist.addAll(waitRepo.findAllByOrderByIdAsc());
    }

    public List<Reservation> all() { return repo.findAllByOrderByIdAsc(); }

    public List<Reservation> forCustomer(Long customerId) {
        return repo.findAllByOrderByIdAsc().stream().filter(r -> customerId.equals(r.getCustomerId())).toList();
    }

    public synchronized List<WaitlistEntry> waitlist() { return new ArrayList<>(waitlist); }

    public synchronized List<WaitlistEntry> waitlistFor(Long customerId) {
        return waitlist.stream().filter(w -> customerId.equals(w.getCustomerId())).toList();
    }

    /** Builds the table ARRAY with live status for one date and time. */
    public synchronized DiningTable[] tables(String date, String time) {
        DiningTable[] arr = new DiningTable[TABLE_COUNT];
        for (int i = 0; i < TABLE_COUNT; i++) {
            String status = bookingKeys.contains(key(i + 1, date, time)) ? "Reserved" : "Available";
            arr[i] = new DiningTable(i + 1, SEATS[i], status);
        }
        return arr;
    }

    public synchronized Reservation reserve(AuthUser u, int tableNo, String date, String time, int guests) {
        if (tableNo < 1 || tableNo > TABLE_COUNT) throw bad("Pick an available table first.");
        if (date == null || date.isBlank() || time == null || time.isBlank()) throw bad("Choose a date and time.");
        if (!OPEN_SLOTS.contains(time)) throw bad("House of Brew is open 6:00 PM to 3:00 AM. Pick a time in that range.");
        if (guests < 1 || guests > SEATS[tableNo - 1])
            throw bad("Table " + tableNo + " seats " + SEATS[tableNo - 1] + ". Choose a larger table.");
        if (personKeys.contains(personKey(u.getCustomerNo(), date, time)))
            throw conflict("You already have a booking at this time.");
        if (bookingKeys.contains(key(tableNo, date, time)))
            throw conflict("That table was just booked. Pick another.");
        Reservation nr = new Reservation(u.getName(), tableNo, date, time, guests);
        nr.assignCustomer(u.getId(), u.getCustomerNo());
        Reservation r = repo.save(nr);
        index(r);
        return r;
    }

    public synchronized Reservation confirm(Long id) {
        Reservation r = get(id);
        if ("Pending".equals(r.getStatus())) { r.setStatus("Confirmed"); r = repo.save(r); }
        return r;
    }

    /** Cancels a booking, frees the table, and gives it to the first matching person on the waitlist. */
    public synchronized Reservation cancel(Long id) {
        Reservation r = get(id);
        if ("Cancelled".equals(r.getStatus()) || "Completed".equals(r.getStatus())) return r;
        r.setStatus("Cancelled");
        repo.save(r);
        unindex(r);
        promoteWaitlist(r);
        return r;
    }

    /** A customer cancels their own booking. Only possible before the staff confirms it. */
    public synchronized Reservation cancelOwn(AuthUser u, Long id) {
        Reservation r = get(id);
        if (!u.getId().equals(r.getCustomerId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "That is not your reservation.");
        if (!"Pending".equals(r.getStatus()))
            throw conflict("Too late to cancel: this booking is already " + r.getStatus().toLowerCase() + ".");
        return cancel(id);
    }

    /** Staff cancel: only allowed before the booking is confirmed. */
    public synchronized Reservation cancelPending(Long id) {
        Reservation r = get(id);
        if ("Confirmed".equals(r.getStatus()))
            throw conflict("This booking is already confirmed. Use Mark as available when the guests are done.");
        if ("Completed".equals(r.getStatus()))
            throw conflict("This booking is already finished.");
        return cancel(id);
    }

    /** Guests are done eating: the table becomes available again (and the waitlist gets the slot). */
    public synchronized Reservation complete(Long id) {
        Reservation r = get(id);
        if (!"Confirmed".equals(r.getStatus()))
            throw bad("Only a confirmed booking can be marked as available.");
        r.setStatus("Completed");
        repo.save(r);
        unindex(r);
        promoteWaitlist(r);
        return r;
    }

    private void promoteWaitlist(Reservation r) {
        Iterator<WaitlistEntry> it = waitlist.iterator();
        while (it.hasNext()) {
            WaitlistEntry w = it.next();
            if (w.getBookingDate().equals(r.getBookingDate()) && w.getBookingTime().equals(r.getBookingTime())
                    && w.getGuests() <= SEATS[r.getTableNo() - 1]) {
                it.remove();                       // dequeue from the waitlist
                waitRepo.delete(w);
                Reservation promoted = new Reservation(w.getName(), r.getTableNo(), w.getBookingDate(), w.getBookingTime(), w.getGuests());
                promoted.assignCustomer(w.getCustomerId(), w.getCustomerNo());
                index(repo.save(promoted));
                break;
            }
        }
    }

    public synchronized WaitlistEntry join(AuthUser u, String date, String time, int guests) {
        if (date == null || time == null) throw bad("Choose a date and time.");
        if (!OPEN_SLOTS.contains(time)) throw bad("House of Brew is open 6:00 PM to 3:00 AM. Pick a time in that range.");
        for (DiningTable t : tables(date, time))
            if (t.getStatus().equals("Available") && t.getSeats() >= guests)
                throw conflict("A table is free for that party. Please reserve it instead.");
        WaitlistEntry nw = new WaitlistEntry(u.getName(), date, time, guests);
        nw.assignCustomer(u.getId(), u.getCustomerNo());
        WaitlistEntry w = waitRepo.save(nw);
        waitlist.addLast(w);                       // enqueue
        return w;
    }

    public synchronized int bookingKeyCount() { return bookingKeys.size(); }
    public synchronized int waitlistSize() { return waitlist.size(); }

    private void index(Reservation r) {
        bookingKeys.add(key(r.getTableNo(), r.getBookingDate(), r.getBookingTime()));
        personKeys.add(personKey(r.getCustomerNo(), r.getBookingDate(), r.getBookingTime()));
    }
    private void unindex(Reservation r) {
        bookingKeys.remove(key(r.getTableNo(), r.getBookingDate(), r.getBookingTime()));
        personKeys.remove(personKey(r.getCustomerNo(), r.getBookingDate(), r.getBookingTime()));
    }
    private String key(int table, String date, String time) { return table + "|" + date + "|" + time; }
    private String personKey(String customerNo, String date, String time) {
        return (customerNo == null ? "" : customerNo.trim()) + "|" + date + "|" + time;
    }
    private Reservation get(Long id) {
        return repo.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found."));
    }
    private ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private ResponseStatusException conflict(String m) { return new ResponseStatusException(HttpStatus.CONFLICT, m); }
}
