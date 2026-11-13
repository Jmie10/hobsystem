package com.hob.hobsystem.config;

import com.hob.hobsystem.service.MenuService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final MenuService menu;
    public DataSeeder(MenuService menu) { this.menu = menu; }

    @Override
    public void run(String... args) { menu.seedIfEmpty(); }
}
