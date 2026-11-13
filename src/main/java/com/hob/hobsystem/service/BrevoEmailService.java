package com.hob.hobsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Sends email through Brevo's HTTPS API (port 443), so it works on hosts that block SMTP ports
 * (for example Render's free plan). Active only when the BREVO_API_KEY environment variable is set.
 * Do NOT set SPRING_MAIL_HOST on the same server, or two primary email services will conflict.
 */
@Service
@Primary
@ConditionalOnProperty(name = "brevo.api-key")
public class BrevoEmailService implements EmailService {

    @Value("${brevo.api-key}")
    private String apiKey;
    @Value("${brevo.sender-email}")
    private String senderEmail;
    @Value("${brevo.sender-name:House of Brew}")
    private String senderName;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public void send(String to, String subject, String body) {
        String json = "{\"sender\":{\"name\":" + q(senderName) + ",\"email\":" + q(senderEmail) + "},"
                + "\"to\":[{\"email\":" + q(to) + "}],"
                + "\"subject\":" + q(subject) + ","
                + "\"textContent\":" + q(body) + "}";
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(15))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                // Logged, not thrown: the "forgot password" answer must look the same whether or not the email exists.
                System.err.println("[Brevo] Email failed (HTTP " + res.statusCode() + "): " + res.body());
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.err.println("[Brevo] Email interrupted: " + ie);
        } catch (Exception e) {
            System.err.println("[Brevo] Email failed: " + e);
        }
    }

    /** Wraps a string as a JSON string literal with proper escaping. */
    private static String q(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
