package com.hob.hobsystem.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

@Service
public class UndoService {
    public record UndoAction(String type, Long id) {}

    /** STACK: one undo stack per browser (client id). push = new action, pop = Undo. */
    private final Map<String, Deque<UndoAction>> stacks = new HashMap<>();

    public synchronized void push(String client, String type, Long id) {
        if (client == null || client.isBlank()) return;
        stacks.computeIfAbsent(client, k -> new ArrayDeque<>()).push(new UndoAction(type, id));
    }

    public synchronized UndoAction pop(String client) {
        Deque<UndoAction> d = stacks.get(client);
        return (d == null || d.isEmpty()) ? null : d.pop();
    }

    public synchronized int totalSize() {
        int n = 0;
        for (Deque<UndoAction> d : stacks.values()) n += d.size();
        return n;
    }
}
