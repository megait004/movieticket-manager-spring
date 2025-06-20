package com.movieticket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class Index {

    private final PageController pageController;

    public Index(PageController pageController) {
        this.pageController = pageController;
    }

    @GetMapping("/")
    public String overview(HttpServletRequest request, Model model) {
        pageController.addAuthStatus(request, model);
        if (isHtmxRequest(request)) {
            return "index :: section";
        }
        return "index";
    }

    private boolean isHtmxRequest(HttpServletRequest request) {
        return request.getHeader("HX-Request") != null;
    }
}
