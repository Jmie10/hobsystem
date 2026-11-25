package com.hob.hobsystem.util;

import java.util.*;

/**
 * GRAPH: undirected, weighted, stored as an adjacency list (Map of Map).
 * Each vertex is a menu item. An edge A-B means "A and B were ordered together";
 * its weight is how many orders contained both.
 */
public class WeightedGraph<T> {
    private final Map<T, Map<T, Integer>> adj = new HashMap<>();

    /** Adds 1 to the weight of the edge a-b (creates it if missing). */
    public void addEdge(T a, T b) {
        if (a.equals(b)) return;
        adj.computeIfAbsent(a, k -> new HashMap<>()).merge(b, 1, Integer::sum);
        adj.computeIfAbsent(b, k -> new HashMap<>()).merge(a, 1, Integer::sum);
    }

    /** Subtracts 1 from the weight of the edge a-b; removes the edge when it reaches 0. */
    public void removeEdge(T a, T b) {
        dec(a, b);
        dec(b, a);
    }

    private void dec(T a, T b) {
        Map<T, Integer> m = adj.get(a);
        if (m == null) return;
        Integer w = m.get(b);
        if (w == null) return;
        if (w <= 1) m.remove(b); else m.put(b, w - 1);
        if (m.isEmpty()) adj.remove(a);
    }

    public int weight(T a, T b) {
        Map<T, Integer> m = adj.get(a);
        return m == null ? 0 : m.getOrDefault(b, 0);
    }

    public int vertexCount() { return adj.size(); }

    /** Neighbors of one vertex, strongest edge first. */
    public List<Map.Entry<T, Integer>> neighbors(T v) {
        Map<T, Integer> m = adj.get(v);
        if (m == null) return List.of();
        List<Map.Entry<T, Integer>> out = new ArrayList<>(m.entrySet());
        out.sort((x, y) -> y.getValue() - x.getValue());
        return out;
    }

    /**
     * Recommendation: for the given vertices, add up the edge weights to every other vertex,
     * skip the given ones, and return the top {@code limit} by total weight.
     */
    public List<T> recommend(Collection<T> from, int limit) {
        Map<T, Integer> score = new HashMap<>();
        for (T v : from)
            for (Map.Entry<T, Integer> e : neighbors(v))
                if (!from.contains(e.getKey())) score.merge(e.getKey(), e.getValue(), Integer::sum);
        List<Map.Entry<T, Integer>> ranked = new ArrayList<>(score.entrySet());
        ranked.sort((x, y) -> y.getValue() - x.getValue());
        List<T> out = new ArrayList<>();
        for (Map.Entry<T, Integer> e : ranked) { if (out.size() >= limit) break; out.add(e.getKey()); }
        return out;
    }
}
