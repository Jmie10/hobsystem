package com.hob.hobsystem.config;

import com.hob.hobsystem.model.AuthUser;
import com.hob.hobsystem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/** Checks the login token on every /api request and blocks actions the user's role may not do. */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService auth;
    public AuthInterceptor(AuthService auth) { this.auth = auth; }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
        String path = req.getRequestURI();
        if (path.equals("/api/auth/signup") || path.equals("/api/auth/login")
                || path.equals("/api/auth/forgot") || path.equals("/api/auth/reset")) return true;
        String header = req.getHeader("Authorization");
        String token = header == null ? null : header.replace("Bearer", "").trim();
        AuthUser user = auth.user(token);
        if (user == null) return deny(res, 401, "Please log in first.");
        req.setAttribute("auth", user);
        if (!user.canAccess(req.getMethod(), path))     // POLYMORPHISM: each account type decides for itself
            return deny(res, 403, "Your account cannot do this action.");
        return true;
    }

    private boolean deny(HttpServletResponse res, int status, String msg) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"message\":\"" + msg + "\"}");
        return false;
    }
}
