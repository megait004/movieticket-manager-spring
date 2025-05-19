package com.movieticket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class Index {

    @GetMapping("/")
    public String overview(HttpServletRequest request) {
        return "index";
    }
}
