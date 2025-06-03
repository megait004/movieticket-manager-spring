package com.movieticket.controller.api;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ProfileController {

    private final JdbcTemplate jdbcTemplate;

    public ProfileController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping(value = "/api/profile/update")
    public String updateProfile(@RequestBody(required = false) Map<String, String> profileData,
            @CookieValue(name = "user_id", required = false) String adminId,
            Model model) {
        if (adminId == null) {
            model.addAttribute("message", "Vui lòng đăng nhập");
            return "components/error-div :: error";
        }

        String adminRoleQuery = "SELECT role FROM USERS WHERE uid = ?";
        String userRole = jdbcTemplate.queryForObject(adminRoleQuery, String.class, adminId);
        boolean isAdmin = "admin".equals(userRole);

        String targetUserId = profileData.get("uid");
        if (targetUserId == null) {
            targetUserId = adminId;
        } else if (!isAdmin) {
            model.addAttribute("message", "Unauthorized");
            return "components/error-div :: error";
        }

        String email = profileData.get("email");
        String fullname = profileData.get("fullname");
        String role = profileData.get("role");
        String active = profileData.get("active");

        if (!StringUtils.hasText(email) || !isValidEmail(email)) {
            model.addAttribute("message", "Email không hợp lệ");
            return "components/error-div :: error";
        }

        try {
            String checkSql = "SELECT COUNT(*) FROM USERS WHERE email = ? AND uid != ?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, email, targetUserId);

            if (count > 0) {
                model.addAttribute("message", "Email đã tồn tại");
                return "components/error-div :: error";
            }

            String updateSql;
            Object[] params;

            if (isAdmin) {
                updateSql = "UPDATE USERS SET email = ?, fullname = ?, role = ?, active = ?, updated_at = CURRENT_TIMESTAMP WHERE uid = ?";
                params = new Object[]{email, fullname, role, active, targetUserId};
            } else {
                updateSql = "UPDATE USERS SET email = ?, fullname = ?, updated_at = CURRENT_TIMESTAMP WHERE uid = ?";
                params = new Object[]{email, fullname, targetUserId};
            }

            jdbcTemplate.update(updateSql, params);

            model.addAttribute("message", "Cập nhật thông tin thành công");
            return "components/ok-div :: ok";
        } catch (DataAccessException e) {
            model.addAttribute("message", "Lỗi cập nhật: " + e.getMessage());
            return "components/error-div :: error";
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}
