package com.hob.hobsystem.controller;

import com.hob.hobsystem.model.MenuItem;
import com.hob.hobsystem.model.MenuNode;
import com.hob.hobsystem.service.MenuService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menu")
public class MenuController {
    private final MenuService menu;
    public MenuController(MenuService menu) { this.menu = menu; }

    @GetMapping
    public MenuNode tree() { return menu.tree(); }

    @PostMapping("/items")
    public MenuItem add(@RequestBody Requests.ItemReq r) { return menu.add(r.name(), r.category(), r.price()); }

    @PostMapping("/items/{id}/toggle")
    public void toggle(@PathVariable("id") Long id) { menu.toggle(id); }

    @PostMapping("/items/{id}/update")
    public MenuItem update(@PathVariable("id") Long id, @RequestBody Requests.ItemReq r) {
        return menu.update(id, r.name(), r.category(), r.price());
    }

    @DeleteMapping("/items/{id}")
    public void delete(@PathVariable("id") Long id) { menu.delete(id); }
}
