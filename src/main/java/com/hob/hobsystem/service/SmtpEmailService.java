package com.hob.hobsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends a real email only when spring.mail.host is set. Otherwise ConsoleEmailService is used. */
@Service
@Primary
@ConditionalOnProperty(name = "spring.mail.host")
public class SmtpEmailService implements EmailService {
    private final JavaMailSender sender;
    @Value("${spring.mail.username:}")
    private String from;

    public SmtpEmailService(JavaMailSender sender) { this.sender = sender; }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(to);
        m.setSubject(subject);
        m.setText(body);
        sender.send(m);
    }
}