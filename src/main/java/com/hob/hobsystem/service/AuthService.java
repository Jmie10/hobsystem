package com.hob.hobsystem.service;

import com.hob.hobsystem.model.AuthUser;
import com.hob.hobsystem.model.Customer;
import com.hob.hobsystem.model.CustomerAccount;
import com.hob.hobsystem.model.OwnerAccount;
import com.hob.hobsystem.model.StaffAccount;
import com.hob.hobsystem.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    public record Session(String token, AuthUser user) {}

    /** A password reset code waiting to be used. */
    private static class ResetCode {
        final String code;
        final long expires;
        int attempts = 0;
        ResetCode(String code, long expires) { this.code = code; this.expires = expires; }
    }

    private final CustomerRepository repo;
    private final EmailService mail;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, AuthUser> sessions = new ConcurrentHashMap<>();
    private final Map<String, ResetCode> resets = new ConcurrentHashMap<>();

    /** Wrong password counter per account. 3 wrong tries lock the login for 5 minutes. */
    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_SECONDS = 300;
    private static class Attempt { int count = 0; long lockedUntil = 0; }
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public AuthService(CustomerRepository repo, EmailService mail) {
        this.repo = repo;
        this.mail = mail;
        seed("staff", "STAFF", "House of Brew Staff", "staff123", "STAFF");
        seed("owner", "ADMIN", "Owner", "owner123", "OWNER");
    }

    /** Creates a customer account and generates the Customer ID automatically (001, 002, ... 009). */
    public synchronized Session signUp(String firstName, String middleName, String lastName, String email,
                                       String contact, String password) {
        String f = cleanName(firstName, true, "first name");
        String m = cleanName(middleName, false, "middle name");
        String l = cleanName(lastName, true, "last name");
        String e = normalizeEmail(email);
        if (e == null) throw bad("Enter a valid email address.");
        if (contact == null || !contact.matches("\\d{11}")) throw bad("The contact number must be exactly 11 digits.");
        checkPassword(password);
        if (repo.findByContactNumber(contact) != null)
            throw conflict("That contact number is already registered. Please log in.");
        if (repo.findByEmail(e) != null)
            throw conflict("That email is already registered. Please log in.");
        String full = f + (m.isEmpty() ? "" : " " + m) + " " + l;
        Customer c = new Customer(full, contact, hash(password), "CUSTOMER");
        c.setNames(f, m, l);
        c.setEmail(e);
        c = repo.save(c);
        c.setCustomerNo(String.format("%03d", c.getId()));   // automatic ID from the row number
        c = repo.save(c);
        return open(c);
    }

    /** Log in with the Customer ID, the email, or the contact number. */
    public Session login(String idOrContact, String password) {
        if (idOrContact == null || password == null) throw bad("Enter your ID, email or contact number and the password.");
        String key = idOrContact.trim();
        Customer c = repo.findByContactNumber(key);
        if (c == null) c = repo.findByCustomerNo(key);
        if (c == null) c = repo.findByEmail(key.toLowerCase());
        String attemptKey = c != null ? "c" + c.getId() : "x" + key.toLowerCase();
        Attempt a = attempts.computeIfAbsent(attemptKey, k -> new Attempt());
        synchronized (a) {
            long now = System.currentTimeMillis();
            if (a.lockedUntil > now) throw new LockedException((int) Math.ceil((a.lockedUntil - now) / 1000.0));
            if (c == null || !verify(password, c.getPasswordHash())) {
                a.count++;
                if (a.count >= MAX_ATTEMPTS) {          // 3 wrong tries: locked for 5 minutes
                    a.count = 0;
                    a.lockedUntil = now + LOCK_SECONDS * 1000L;
                    throw new LockedException(LOCK_SECONDS);
                }
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Wrong ID, email, contact number or password. " + (MAX_ATTEMPTS - a.count) + " attempt(s) left.");
            }
            a.count = 0;
            a.lockedUntil = 0;
        }
        return open(c);
    }

    /** Step 1 of "forgot password": sends a 6-digit code to the email. The answer is the same whether or not the email exists. */
    public void requestReset(String email) {
        String e = normalizeEmail(email);
        if (e == null) throw bad("Enter a valid email address.");
        Customer c = repo.findByEmail(e);
        if (c == null) return;
        String code = String.format("%06d", random.nextInt(1_000_000));
        resets.put(e, new ResetCode(code, System.currentTimeMillis() + 15 * 60 * 1000));
        mail.send(e, "House of Brew password reset",
            "Hello " + c.getName() + ",\n\nYour House of Brew password reset code is " + code
            + ".\nIt expires in 15 minutes. If you did not ask for this, you can ignore this message.");
    }

    /** Step 2 of "forgot password": checks the code and saves the new password. */
    public synchronized void resetPassword(String email, String code, String newPassword) {
        checkPassword(newPassword);
        String e = normalizeEmail(email);
        ResetCode r = e == null ? null : resets.get(e);
        String wrong = "That code is wrong or has expired. Ask for a new one.";
        if (r == null || System.currentTimeMillis() > r.expires) throw bad(wrong);
        if (++r.attempts > 5) { resets.remove(e); throw bad("Too many wrong tries. Ask for a new code."); }
        if (!r.code.equals(code == null ? "" : code.trim())) throw bad(wrong);
        Customer c = repo.findByEmail(e);
        if (c == null) throw bad(wrong);
        c.setPasswordHash(hash(newPassword));
        repo.save(c);
        resets.remove(e);
        attempts.remove("c" + c.getId());          // a reset also clears a lock
        final Long id = c.getId();
        sessions.values().removeIf(u -> u.getId().equals(id));   // log out everywhere
    }

    public synchronized void changePassword(Long userId, String current, String next) {
        checkPassword(next);
        Customer c = find(userId);
        if (current == null || !verify(current, c.getPasswordHash())) throw bad("The current password is wrong.");
        c.setPasswordHash(hash(next));
        repo.save(c);
    }

    public synchronized void changeEmail(Long userId, String email, String current) {
        String e = normalizeEmail(email);
        if (e == null) throw bad("Enter a valid email address.");
        Customer c = find(userId);
        if (current == null || !verify(current, c.getPasswordHash())) throw bad("The current password is wrong.");
        Customer other = repo.findByEmail(e);
        if (other != null && !other.getId().equals(userId)) throw conflict("That email is already used by another account.");
        c.setEmail(e);
        repo.save(c);
    }

    public Map<String, Object> account(Long userId) {
        Customer c = find(userId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("customerNo", c.getCustomerNo());
        m.put("name", c.getName());
        m.put("firstName", c.getFirstName());
        m.put("middleName", c.getMiddleName());
        m.put("lastName", c.getLastName());
        m.put("email", c.getEmail());
        m.put("contactNumber", c.getContactNumber());
        m.put("role", c.getUserRole());
        return m;
    }

    public AuthUser user(String token) { return token == null ? null : sessions.get(token); }

    public void logout(String token) { if (token != null) sessions.remove(token); }

    /** Account records for the owner. */
    public List<Map<String, Object>> customers() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Customer c : repo.findByUserRoleOrderByIdAsc("CUSTOMER")) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("customerNo", c.getCustomerNo());
            m.put("name", c.getName());
        m.put("firstName", c.getFirstName());
        m.put("middleName", c.getMiddleName());
        m.put("lastName", c.getLastName());
            m.put("contactNumber", c.getContactNumber());
            m.put("email", c.getEmail());
            out.add(m);
        }
        return out;
    }

    private Session open(Customer c) {
        byte[] t = new byte[24];
        random.nextBytes(t);
        String token = HexFormat.of().formatHex(t);
        AuthUser u = accountFor(c);
        sessions.put(token, u);
        return new Session(token, u);
    }

    /** POLYMORPHISM: the right kind of account object is created from the stored role. */
    private AuthUser accountFor(Customer c) {
        return switch (c.getUserRole()) {
            case "ADMIN" -> new OwnerAccount(c.getId(), c.getCustomerNo(), c.getName());
            case "STAFF" -> new StaffAccount(c.getId(), c.getCustomerNo(), c.getName());
            default -> new CustomerAccount(c.getId(), c.getCustomerNo(), c.getName());
        };
    }

    private void seed(String contact, String role, String name, String password, String no) {
        if (repo.findByContactNumber(contact) != null) return;
        Customer c = new Customer(name, contact, hash(password), role);
        c.setCustomerNo(no);
        repo.save(c);
    }

    private Customer find(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        String e = email.trim().toLowerCase();
        return (e.length() <= 120 && e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) ? e : null;
    }

    private String hash(String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(pbkdf2(password, salt));
    }

    private boolean verify(String password, String stored) {
        if (stored == null || !stored.contains(":")) return false;
        String[] parts = stored.split(":");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);
        return MessageDigest.isEqual(expected, pbkdf2(password, salt));
    }

    private byte[] pbkdf2(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    /** A new password must not be weak: at least 8 characters with letters and numbers. */
    private void checkPassword(String p) {
        if (p == null || p.length() < 8 || !p.matches(".*[A-Za-z].*") || !p.matches(".*\\d.*"))
            throw bad("The password is too weak. Use at least 8 characters with letters and numbers.");
    }

    private String cleanName(String s, boolean required, String label) {
        String v = s == null ? "" : s.trim().replaceAll("\\s+", " ");
        if (v.isEmpty()) {
            if (required) throw bad("Enter your " + label + ".");
            return "";
        }
        if (!v.matches("^\\p{L}[\\p{L} .'-]{0,39}$"))
            throw bad("The " + label + " can only have letters, spaces, hyphens and periods.");
        return v;
    }

    private ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private ResponseStatusException conflict(String m) { return new ResponseStatusException(HttpStatus.CONFLICT, m); }
}
