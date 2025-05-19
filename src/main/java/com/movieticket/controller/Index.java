package com.movieticket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class Index {

    @GetMapping("/")
    public String overview(HttpServletRequest request) {
        // Kiểm tra nếu là yêu cầu htmx
        if (isHtmxRequest(request)) {
            return "index :: section";
        }
        return "index";
    }

    /**
     * Kiểm tra xem yêu cầu có phải từ htmx không
     */
    private boolean isHtmxRequest(HttpServletRequest request) {
        return request.getHeader("HX-Request") != null;
    }
}
