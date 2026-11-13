package com.hob.hobsystem.config;

import com.hob.hobsystem.service.LockedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Turns a LockedException into a JSON answer with the seconds left, so the page can show a countdown. */
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> locked(LockedException e) {
        int s = e.getSeconds();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Too many wrong attempts. Try again in " + (s / 60) + ":" + String.format("%02d", s % 60) + ".");
        body.put("retryAfter", s);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }

    /** Sends our readable message (wrong password, table already booked, ...) so the page shows it instead of "Request failed". */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> status(ResponseStatusException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", e.getReason() != null ? e.getReason() : "Something went wrong. Please try again.");
        return ResponseEntity.status(e.getStatusCode()).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Some details are missing or incorrect. Please check the form and try again.");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> unexpected(RuntimeException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Something went wrong on our side. Please try again in a moment.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
