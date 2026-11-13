package com.hob.hobsystem.service;

/**
 * ABSTRACTION: says that the system can send an email, without saying how.
 * POLYMORPHISM: ConsoleEmailService prints the message in the Eclipse console (default).
 * SmtpEmailService (optional, see the optional-email folder) sends a real email.
 */
public interface EmailService {
    void send(String to, String subject, String body);
}
