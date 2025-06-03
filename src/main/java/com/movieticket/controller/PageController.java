package com.movieticket.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PageController implements ErrorController {

    private final JdbcTemplate jdbcTemplate;

    public PageController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addAuthStatus(HttpServletRequest request, Model model) {
        Cookie[] cookies = request.getCookies();
        String userId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("user_id".equals(cookie.getName())) {
                    userId = cookie.getValue();
                    break;
                }
            }
        }
        model.addAttribute("isAuthenticated", userId != null);

        if (userId != null) {
            String roleQuery = "SELECT uid, role FROM USERS WHERE uid = ?";
            var user = jdbcTemplate.queryForMap(roleQuery, userId);
            model.addAttribute("user", user);
        }
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        addAuthStatus(request, model);
        if (isHtmxRequest(request)) {
            return "pages/login :: section";
        }
        return "pages/login";
    }

    @GetMapping("/register")
    public String register(HttpServletRequest request, Model model) {
        addAuthStatus(request, model);
        if (isHtmxRequest(request)) {
            return "pages/register :: section";
        }
        return "pages/register";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Model model) {
        addAuthStatus(request, model);
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

    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        addAuthStatus(request, model);

        Cookie[] cookies = request.getCookies();
        String userId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("user_id".equals(cookie.getName())) {
                    userId = cookie.getValue();
                    break;
                }
            }
        }

        if (userId == null) {
            return "redirect:/login";
        }

        String roleQuery = "SELECT role FROM USERS WHERE uid = ?";
        String userRole = jdbcTemplate.queryForObject(roleQuery, String.class, userId);
        if ("admin".equals(userRole)) {
            return "redirect:/dashboard";
        }

        String sql = "SELECT uid, username, email, fullname, "
                + "strftime('%d/%m/%Y', created_at) as created_at_formatted "
                + "FROM USERS WHERE uid = ?";
        try {
            var users = jdbcTemplate.queryForList(sql, userId);
            if (!users.isEmpty()) {
                model.addAttribute("user", users.get(0));
            }
        } catch (DataAccessException e) {
            model.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }

        if (isHtmxRequest(request)) {
            return "pages/profile :: section";
        }
        return "pages/profile";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        addAuthStatus(request, model);

        Cookie[] cookies = request.getCookies();
        String userId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("user_id".equals(cookie.getName())) {
                    userId = cookie.getValue();
                    break;
                }
            }
        }

        if (userId == null) {
            return "redirect:/login";
        }

        String roleQuery = "SELECT role FROM USERS WHERE uid = ?";
        String userRole = jdbcTemplate.queryForObject(roleQuery, String.class, userId);
        if (!"admin".equals(userRole)) {
            return "redirect:/";
        }

        String movieCountSql = "SELECT COUNT(*) FROM MOVIES";
        String screeningCountSql = "SELECT COUNT(*) FROM SCREENINGS";
        String userCountSql = "SELECT COUNT(*) FROM USERS";
        String ticketCountSql = "SELECT COUNT(*) FROM TICKETS";

        model.addAttribute("totalMovies", jdbcTemplate.queryForObject(movieCountSql, Integer.class));
        model.addAttribute("totalScreenings", jdbcTemplate.queryForObject(screeningCountSql, Integer.class));
        model.addAttribute("totalUsers", jdbcTemplate.queryForObject(userCountSql, Integer.class));
        model.addAttribute("totalTickets", jdbcTemplate.queryForObject(ticketCountSql, Integer.class));

        if (isHtmxRequest(request)) {
            return "pages/admin/dashboard :: section";
        }
        return "pages/admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String userManager(HttpServletRequest request, Model model) {
        addAuthStatus(request, model);

        Cookie[] cookies = request.getCookies();
        String userId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("user_id".equals(cookie.getName())) {
                    userId = cookie.getValue();
                    break;
                }
            }
        }

        if (userId == null) {
            return "redirect:/login";
        }

        String roleQuery = "SELECT role FROM USERS WHERE uid = ?";
        String userRole = jdbcTemplate.queryForObject(roleQuery, String.class, userId);
        if (!"admin".equals(userRole)) {
            return "redirect:/";
        }

        String sql = "SELECT uid, username, email, fullname, role, active, " +
                    "strftime('%d/%m/%Y', created_at) as created_at_formatted, " +
                    "strftime('%d/%m/%Y', updated_at) as updated_at_formatted " +
                    "FROM USERS ORDER BY created_at DESC";
        try {
            var users = jdbcTemplate.queryForList(sql);
            model.addAttribute("users", users);
        } catch (DataAccessException e) {
            model.addAttribute("error", "System error: " + e.getMessage());
        }

        if (isHtmxRequest(request)) {
            return "pages/admin/users :: section";
        }
        return "pages/admin/users";
    }

    @GetMapping("/admin/users/edit/{uid}")
    public String editUser(@PathVariable(name = "uid") String uid, HttpServletRequest request, Model model) {
        addAuthStatus(request, model);

        // Verify admin access
        if (!isAdmin(request)) {
            return "redirect:/";
        }

        String sql = "SELECT * FROM USERS WHERE uid = ?";
        try {
            var user = jdbcTemplate.queryForMap(sql, uid);
            model.addAttribute("user", user);
            return "components/dashboard/modal-edit-user :: modal";
        } catch (DataAccessException e) {
            model.addAttribute("error", "User not found");
            return "components/error-div :: error";
        }
    }

    @GetMapping("/admin/tickets")
    public String ticketManager(HttpServletRequest request, Model model) {
        addAuthStatus(request, model);

        if (!isAdmin(request)) {
            return "redirect:/";
        }

        String sql = """
            SELECT t.id, t.seat_number,
                   u.username, u.fullname,
                   m.title as movie_title,
                   s.room_number, s.price,
                   strftime('%d/%m/%Y %H:%M', s.start_time) as screening_time,
                   strftime('%d/%m/%Y %H:%M', t.created_at) as booking_time
            FROM TICKETS t
            JOIN USERS u ON t.uid = u.uid
            JOIN SCREENINGS s ON t.screening_id = s.id
            JOIN MOVIES m ON s.movie_id = m.id
            ORDER BY t.created_at DESC
        """;

        try {
            var tickets = jdbcTemplate.queryForList(sql);
            model.addAttribute("tickets", tickets);
        } catch (DataAccessException e) {
            model.addAttribute("error", "System error: " + e.getMessage());
        }

        if (isHtmxRequest(request)) {
            return "pages/admin/tickets :: section";
        }
        return "pages/admin/tickets";
    }

    @GetMapping("/admin/screenings")
    public String screeningManager(HttpServletRequest request, Model model) {
        addAuthStatus(request, model);

        if (!isAdmin(request)) {
            return "redirect:/";
        }

        String sql = """
            SELECT s.id, s.room_number, s.price, s.available_seats,
                   m.title as movie_title, m.duration, m.movie_type,
                   strftime('%d/%m/%Y %H:%M', s.start_time) as screening_time,
                   strftime('%d/%m/%Y %H:%M', datetime(s.start_time, '+' || m.duration || ' minutes')) as end_time
            FROM SCREENINGS s
            JOIN MOVIES m ON s.movie_id = m.id
            ORDER BY s.start_time ASC
        """;

        try {
            var screenings = jdbcTemplate.queryForList(sql);
            model.addAttribute("screenings", screenings);

            // Get movies for the add screening form
            String moviesSql = "SELECT id, title FROM MOVIES WHERE status = 'NOW_SHOWING' ORDER BY title";
            var movies = jdbcTemplate.queryForList(moviesSql);
            model.addAttribute("movies", movies);
        } catch (DataAccessException e) {
            model.addAttribute("error", "System error: " + e.getMessage());
        }

        if (isHtmxRequest(request)) {
            return "pages/admin/screenings :: section";
        }
        return "pages/admin/screenings";
    }

    @GetMapping("/admin/movies")
    public String movieManager(HttpServletRequest request, Model model) {
        addAuthStatus(request, model);

        if (!isAdmin(request)) {
            return "redirect:/";
        }

        String sql = """
            SELECT id, title, description, director, movie_cast, duration,
                   movie_type, language, rating, trailer_url, poster_url,
                   strftime('%d/%m/%Y', release_date) as formatted_release_date,
                   status
            FROM MOVIES
            ORDER BY release_date DESC
        """;

        try {
            var movies = jdbcTemplate.queryForList(sql);
            model.addAttribute("movies", movies);
        } catch (DataAccessException e) {
            model.addAttribute("error", "System error: " + e.getMessage());
        }

        if (isHtmxRequest(request)) {
            return "pages/admin/movies :: section";
        }
        return "pages/admin/movies";
    }

    @GetMapping("/admin/movies/add")
    public String addMovie(HttpServletRequest request, Model model) {
        if (!isAdmin(request)) {
            return "redirect:/";
        }
        return "components/dashboard/modal-add-movie :: modal";
    }

    private boolean isHtmxRequest(HttpServletRequest request) {
        return request.getHeader("HX-Request") != null;
    }

    private boolean isAdmin(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String userId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("user_id".equals(cookie.getName())) {
                    userId = cookie.getValue();
                    break;
                }
            }
        }
        if (userId == null) return false;

        String roleQuery = "SELECT role FROM USERS WHERE uid = ?";
        String userRole = jdbcTemplate.queryForObject(roleQuery, String.class, userId);
        return "admin".equals(userRole);
    }
}
