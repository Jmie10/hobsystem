package com.hob.hobsystem.config;

import com.hob.hobsystem.service.LockedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
