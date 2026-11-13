package com.hob.hobsystem.service;

import org.springframework.stereotype.Service;

/** Default: no mail server is needed. The email is printed in the Eclipse Console so you can copy the code. */
@Service
public class ConsoleEmailService implements EmailService {
    @Override
    public void send(String to, String subject, String body) {
        System.out.println();
        System.out.println("=========== EMAIL (printed here because no mail server is set up) ===========");
        System.out.println("To:      " + to);
        System.out.println("Subject: " + subject);
        System.out.println(body);
        System.out.println("=============================================================================");
        System.out.println();
    }
}
