package com.hob.hobsystem.controller;

import com.hob.hobsystem.service.RecommendService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RecommendController {
    private final RecommendService rec;
    public RecommendController(RecommendService rec) { this.rec = rec; }

    /** GET /api/recommend?items=Latte,Cheese Stick  ->  names of items often ordered with those. */
    @GetMapping("/recommend")
    public List<String> recommend(@RequestParam(name = "items", defaultValue = "") String items) {
        List<String> names = Arrays.stream(items.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return rec.goesWith(names, 4);
    }
}
