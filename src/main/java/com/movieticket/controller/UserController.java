package com.movieticket.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {

    private final DataSource dataSource;

    public UserController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public String listUsers(Model model) {
        List<Map<String, Object>> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uid, username, email, role, active FROM USERS")) {

            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getLong("uid"));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("role", rs.getString("role"));
                user.put("active", rs.getInt("active"));
                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        model.addAttribute("users", users);
        return "users/list";
    }
}
