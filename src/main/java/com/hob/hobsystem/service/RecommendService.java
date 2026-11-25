package com.hob.hobsystem.service;

import com.hob.hobsystem.model.OrderEntity;
import com.hob.hobsystem.model.OrderLine;
import com.hob.hobsystem.repository.OrderRepository;
import com.hob.hobsystem.util.WeightedGraph;
import org.springframework.stereotype.Service;

import java.util.*;

/** Builds the "ordered together" GRAPH from past orders and answers "goes well with" questions. */
@Service
public class RecommendService {
    private final WeightedGraph<String> graph = new WeightedGraph<>();

    public RecommendService(OrderRepository repo) {
        for (OrderEntity o : repo.findAllByOrderByIdAsc()) addOrder(o);   // rebuild from the database on startup
    }

    /** Hot/Iced and fries flavor are dropped so "Latte (Hot)" and "Latte (Iced)" count as one vertex. */
    static String base(String name) {
        return name == null ? "" : name.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();
    }

    private static List<String> distinct(OrderEntity o) {
        Set<String> s = new LinkedHashSet<>();
        for (OrderLine l : o.getItems()) { String b = base(l.getName()); if (!b.isEmpty()) s.add(b); }
        return new ArrayList<>(s);
    }

    public synchronized void addOrder(OrderEntity o) {
        List<String> n = distinct(o);
        for (int i = 0; i < n.size(); i++) for (int j = i + 1; j < n.size(); j++) graph.addEdge(n.get(i), n.get(j));
    }

    public synchronized void removeOrder(OrderEntity o) {
        List<String> n = distinct(o);
        for (int i = 0; i < n.size(); i++) for (int j = i + 1; j < n.size(); j++) graph.removeEdge(n.get(i), n.get(j));
    }

    public synchronized List<String> goesWith(List<String> cartItems, int limit) {
        Set<String> from = new LinkedHashSet<>();
        for (String s : cartItems) { String b = base(s); if (!b.isEmpty()) from.add(b); }
        return graph.recommend(from, limit);
    }
}
