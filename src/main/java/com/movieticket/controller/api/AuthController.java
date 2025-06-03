package com.movieticket.controller.api;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

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

                String redirectPath = role.equals("admin") ? "/dashboard" : "/profile";
                response.setHeader("HX-Redirect", redirectPath);

                model.addAttribute("message", "Đăng nhập thành công");
                return "components/ok-div :: ok";
            } else {
                model.addAttribute("message", "Tên đăng nhập hoặc mật khẩu không đúng");
                return "components/error-div :: error";
            }

        } catch (DataAccessException e) {
            model.addAttribute("message", "Lỗi hệ thống: " + e.getMessage());
            return "components/error-div :: error";
        }
    }

    @GetMapping(value = "/api/logout")
    public String logout(HttpServletResponse response, Model model) {
        Cookie userCookie = new Cookie("user_id", "");
        userCookie.setMaxAge(0);
        userCookie.setPath("/");
        response.addCookie(userCookie);
        Cookie roleCookie = new Cookie("user_role", "");
        roleCookie.setMaxAge(0);
        roleCookie.setPath("/");
        response.addCookie(roleCookie);
        response.setHeader("HX-Redirect", "/login");

        model.addAttribute("message", "Đăng xuất thành công");
        return "components/ok-div :: ok";
    }

    @PostMapping(value = "/api/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String register(@RequestBody Map<String, String> registration, Model model, HttpServletResponse response) {
        String email = registration.get("email");
        String username = registration.get("username");
        String password = registration.get("password");

        if (!StringUtils.hasText(email) || !isValidEmail(email)) {
            model.addAttribute("message", "Email không hợp lệ");
            return "components/error-div :: error";
        }

        if (!StringUtils.hasText(username) || username.length() < 3) {
            model.addAttribute("message", "Tên đăng nhập phải có ít nhất 3 ký tự");
            return "components/error-div :: error";
        }

        if (!StringUtils.hasText(password) || password.length() < 6) {
            model.addAttribute("message", "Mật khẩu phải có ít nhất 6 ký tự");
            return "components/error-div :: error";
        }

        try {
            String checkSql = "SELECT COUNT(*) FROM USERS WHERE username = ? OR email = ?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, username, email);

            if (count > 0) {
                model.addAttribute("message", "Tên đăng nhập hoặc email đã tồn tại");
                return "components/error-div :: error";
            }

            String insertSql = "INSERT INTO USERS (username, password, email, role, active) VALUES (?, ?, ?, 'user', 0)";
            jdbcTemplate.update(insertSql, username, password, email);

            response.setHeader("HX-Redirect", "/login");
            model.addAttribute("message", "Đăng ký thành công");
            return "components/ok-div :: ok";
        } catch (DataAccessException e) {
            model.addAttribute("message", "Lỗi đăng ký: " + e.getMessage());
            return "components/error-div :: error";
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}
