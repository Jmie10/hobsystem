package com.hob.hobsystem.service;

/** Thrown when a login is locked after 3 wrong passwords. Carries the seconds left. */
public class LockedException extends RuntimeException {
    private final int seconds;

    public LockedException(int seconds) {
        super("Too many wrong attempts.");
        this.seconds = seconds;
    }

    public int getSeconds() { return seconds; }
}
