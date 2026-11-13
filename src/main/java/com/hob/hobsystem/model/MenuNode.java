package com.hob.hobsystem.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Node of the menu TREE: Menu -> Category -> Item. ENCAPSULATION: children change only through addChild(). */
public class MenuNode {
    private Long id;            // only set on item (leaf) nodes
    private final String name;
    private Integer price;      // only set on item nodes
    private Boolean available;  // only set on item nodes
    private final List<MenuNode> children = new ArrayList<>();

    public MenuNode(String name) { this.name = name; }

    public static MenuNode leaf(MenuItem m) {
        MenuNode n = new MenuNode(m.getName());
        n.id = m.getId(); n.price = m.getPrice(); n.available = m.isAvailable();
        return n;
    }

    public void addChild(MenuNode child) { children.add(child); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getPrice() { return price; }
    public Boolean getAvailable() { return available; }
    public List<MenuNode> getChildren() { return Collections.unmodifiableList(children); }
}
