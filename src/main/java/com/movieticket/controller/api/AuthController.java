package com.movieticket.controller.api;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@Controller
public class AuthController {

    private final JdbcTemplate jdbcTemplate;

    public AuthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping(value = "/api/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String apiLogin(@RequestBody Map<String, String> credentials,
            HttpServletResponse response, Model model) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        String sql = "SELECT uid, username, role, active FROM USERS WHERE username = ? AND password = ?";

        try {
            var users = jdbcTemplate.queryForList(sql, username, password);
            if (!users.isEmpty()) {
                var user = users.get(0);
                int uid = ((Number) user.get("uid")).intValue();
                String role = (String) user.get("role");
                int active = ((Number) user.get("active")).intValue();

                if (active == 0) {
                    model.addAttribute("message", "Tài khoản chưa được kích hoạt");
                    return "components/error-div :: error";
                }

                Cookie userCookie = new Cookie("user_id", String.valueOf(uid));
                userCookie.setMaxAge(86400);
                userCookie.setPath("/");
                response.addCookie(userCookie);

                Cookie roleCookie = new Cookie("user_role", role);
                roleCookie.setMaxAge(86400);
                roleCookie.setPath("/");
                response.addCookie(roleCookie);
                // TODO: change path from this line
                response.setHeader("HX-Redirect", "/");

                return "";
            } else {
                model.addAttribute("message", "Tên đăng nhập hoặc mật khẩu không đúng");
                return "components/error-div :: error";
            }

        } catch (Exception e) {
            model.addAttribute("message", "Lỗi hệ thống: " + e.getMessage());
            return "components/error-div :: error";
        }
    }

    @PostMapping(value = "/api/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String logout(HttpServletResponse response) {
        Cookie userCookie = new Cookie("user_id", "");
        userCookie.setMaxAge(0);
        userCookie.setPath("/");
        response.addCookie(userCookie);
        Cookie roleCookie = new Cookie("user_role", "");
        roleCookie.setMaxAge(0);
        roleCookie.setPath("/");
        response.addCookie(roleCookie);
        response.setHeader("HX-Redirect", "/login");

        return "";
    }
}
