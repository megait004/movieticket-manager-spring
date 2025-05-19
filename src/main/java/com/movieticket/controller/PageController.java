package com.movieticket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.RequestDispatcher;

@Controller
public class PageController implements ErrorController {

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        if (isHtmxRequest(request)) {
            return "pages/login :: section";
        }
        return "pages/login";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                if (isHtmxRequest(request)) {
                    response.setHeader("HX-Retarget", "#content");
                    response.setHeader("HX-Reswap", "innerHTML");
                    return "404 :: section";
                }
                return "404";
            }
        }

        if (isHtmxRequest(request)) {
            response.setHeader("HX-Retarget", "#content");
            response.setHeader("HX-Reswap", "innerHTML");
            return "404 :: section";
        }
        return "404";
    }

    private boolean isHtmxRequest(HttpServletRequest request) {
        return request.getHeader("HX-Request") != null;
    }
}
