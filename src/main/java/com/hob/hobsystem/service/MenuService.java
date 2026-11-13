package com.hob.hobsystem.service;

import com.hob.hobsystem.model.MenuItem;
import com.hob.hobsystem.model.MenuNode;
import com.hob.hobsystem.repository.MenuItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MenuService {
    private final MenuItemRepository repo;

    public MenuService(MenuItemRepository repo) { this.repo = repo; }

    /** TREE: builds Menu -> Categories -> Items from the flat rows in MySQL. */
    public MenuNode tree() {
        MenuNode root = new MenuNode("Menu");
        Map<String, MenuNode> categories = new LinkedHashMap<>();
        for (MenuItem m : repo.findAllByOrderByIdAsc()) {
            MenuNode category = categories.computeIfAbsent(m.getCategory(), k -> {
                MenuNode c = new MenuNode(k);
                root.addChild(c);
                return c;
            });
            category.addChild(MenuNode.leaf(m));
        }
        return root;
    }

    public int categoryCount() { return tree().getChildren().size(); }

    public MenuItem add(String name, String category, int price) {
        try {
            return repo.save(new MenuItem(name, category, price));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a name and a price above zero.");
        }
    }

    /** The owner can change the name, category and price of an item. Old orders keep the price they were ordered at. */
    public MenuItem update(Long id, String name, String category, int price) {
        MenuItem m = repo.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found."));
        try {
            m.setName(name);
            m.setCategory(category);
            m.setPrice(price);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a name and a price above zero.");
        }
        return repo.save(m);
    }

    public void toggle(Long id) {
        MenuItem m = repo.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found."));
        m.setAvailable(!m.isAvailable());
        repo.save(m);
    }

    public void delete(Long id) { repo.deleteById(id); }

    /** Loads the House of Brew menu the first time the app starts. */
    public void seedIfEmpty() {
        if (repo.count() > 0) return;
        List<MenuItem> l = new ArrayList<>();
        String[][] picaPica = {{"Flavored Fries","100"},{"Fries Overload","120"},{"Nachos Overload","120"},
            {"Cheese Sticks","100"},{"Onion Rings","100"}};
        String[][] meals = {{"Beef Tapa","115"},{"Longganisa","115"},{"Porkchop","125"},{"Liempo","125"},
            {"Shanghai","115"},{"Hungarian","115"},{"Chicken","115"},{"Tocino","115"},{"Tonkatsu","125"},{"Spam Silog","125"}};
        for (String[] r : picaPica) l.add(new MenuItem(r[0], "Pica Pica", Integer.parseInt(r[1])));
        for (String[] r : meals) l.add(new MenuItem(r[0], "Meals", Integer.parseInt(r[1])));
        // {name, hot price (0 = none), iced price (0 = none)}
        Object[][] coffee = {{"Latte",85,99},{"Americano",80,99},{"Spanish Latte",85,99},{"Caramel Macchiato",85,99},
            {"Salted Caramel Latte",85,99},{"Vanilla Latte",85,99},{"White Mocha",85,99},{"Dark Mocha",85,99},
            {"Vietnamese",85,99},{"Dirty Matcha",0,105},{"HOB's Drink",0,105}};
        Object[][] nonCoffee = {{"Choco Milk",0,99},{"Black Forest",0,105},{"Strawberry Milk",0,99},{"Blueberry Milk",0,99},
            {"Mixed Berries",0,99},{"Matcha",90,99},{"Strawberry Matcha",0,99},{"Taro",0,99}};
        Object[][] soda = {{"Blueberry Soda",0,90},{"Strawberry Soda",0,90},{"Lychee Soda",0,90},{"Green Apple Soda",0,90}};
        drinks(l, "Coffee Based", coffee);
        drinks(l, "Non Coffee", nonCoffee);
        drinks(l, "Soda", soda);
        String[][] addOns = {{"Egg","20"},{"Rice","25"},{"Sauce/Dip","20"},{"Espresso Shot","30"},{"Syrup/Sauce","30"}};
        for (String[] r : addOns) l.add(new MenuItem(r[0], "Add-ons", Integer.parseInt(r[1])));
        repo.saveAll(l);
    }

    private void drinks(List<MenuItem> l, String category, Object[][] rows) {
        for (Object[] r : rows) {
            int hot = (Integer) r[1], iced = (Integer) r[2];
            if (hot > 0) l.add(new MenuItem(r[0] + " (Hot)", category, hot));
            if (iced > 0) l.add(new MenuItem(r[0] + " (Iced)", category, iced));
        }
    }
}
