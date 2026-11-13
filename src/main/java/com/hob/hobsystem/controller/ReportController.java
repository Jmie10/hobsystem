package com.hob.hobsystem.controller;

import com.hob.hobsystem.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reports;
    public ReportController(ReportService reports) { this.reports = reports; }

    @GetMapping
    public Map<String, Object> get() { return reports.build(); }
}
