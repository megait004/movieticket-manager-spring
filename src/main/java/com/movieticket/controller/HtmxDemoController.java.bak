package com.movieticket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/htmx-demo")
public class HtmxDemoController {

    @GetMapping
    public String demoPage(Model model) {
        model.addAttribute("serverTime", getCurrentTime());
        return "htmx-demo";
    }

    @PostMapping("/click")
    public String handleClick(Model model, HttpServletResponse response) {
        model.addAttribute("serverTime", getCurrentTime());
        model.addAttribute("message", "Bạn đã nhấn nút lúc " + getCurrentTime());

        // Thêm header HX-Trigger để kích hoạt sự kiện timeUpdated
        response.setHeader("HX-Trigger", "timeUpdated");

        return "fragments/message-fragment";
    }

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "Thế giới") String name, Model model) {
        model.addAttribute("name", name);
        return "fragments/greeting-fragment";
    }

    private String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        return now.format(formatter);
    }
}
