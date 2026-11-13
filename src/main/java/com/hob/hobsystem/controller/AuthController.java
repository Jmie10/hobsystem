package com.hob.hobsystem.controller;

import com.hob.hobsystem.model.AuthUser;
import com.hob.hobsystem.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/auth/signup")
    public AuthService.Session signUp(@RequestBody Requests.SignUpReq r) {
        return auth.signUp(r.firstName(), r.middleName(), r.lastName(), r.email(), r.contactNumber(), r.password());
    }

    @PostMapping("/auth/login")
    public AuthService.Session login(@RequestBody Requests.LoginReq r) {
        return auth.login(r.id(), r.password());
    }

    @PostMapping("/auth/logout")
    public void logout(@RequestHeader(name = "Authorization", required = false) String header) {
        auth.logout(header == null ? null : header.replace("Bearer", "").trim());
    }

    @GetMapping("/auth/me")
    public AuthUser me(@RequestAttribute("auth") AuthUser user) { return user; }

    /** Forgot password, step 1: a 6-digit code is emailed. */
    @PostMapping("/auth/forgot")
    public Map<String, String> forgot(@RequestBody Requests.ForgotReq r) {
        auth.requestReset(r.email());
        return Map.of("message", "If that email is registered, a 6-digit code was sent. It expires in 15 minutes.");
    }

    /** Forgot password, step 2: the code and the new password. */
    @PostMapping("/auth/reset")
    public Map<String, String> reset(@RequestBody Requests.ResetReq r) {
        auth.resetPassword(r.email(), r.code(), r.newPassword());
        return Map.of("message", "Password changed. You can log in now.");
    }

    @GetMapping("/account")
    public Map<String, Object> account(@RequestAttribute("auth") AuthUser user) { return auth.account(user.getId()); }

    @PostMapping("/account/password")
    public Map<String, String> changePassword(@RequestBody Requests.ChangePwReq r, @RequestAttribute("auth") AuthUser user) {
        auth.changePassword(user.getId(), r.currentPassword(), r.newPassword());
        return Map.of("message", "Password changed.");
    }

    @PostMapping("/account/email")
    public Map<String, String> changeEmail(@RequestBody Requests.ChangeEmailReq r, @RequestAttribute("auth") AuthUser user) {
        auth.changeEmail(user.getId(), r.email(), r.currentPassword());
        return Map.of("message", "Email saved.");
    }

    /** Customer account records, for the owner. */
    @GetMapping("/customers")
    public List<Map<String, Object>> customers() { return auth.customers(); }
}
