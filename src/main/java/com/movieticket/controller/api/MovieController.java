package com.movieticket.controller.api;

import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final JdbcTemplate jdbcTemplate;

    public MovieController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping
    public ResponseEntity<?> createMovie(@RequestBody Map<String, Object> movie) {
        String sql = """
            INSERT INTO MOVIES (title, description, director, movie_cast, duration,
                              movie_type, language, trailer_url, poster_url, release_date, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try {
            jdbcTemplate.update(sql,
                    movie.get("title"),
                    movie.get("description"),
                    movie.get("director"),
                    movie.get("movie_cast"),
                    movie.get("duration"),
                    movie.get("movie_type"),
                    movie.get("language"),
                    movie.get("trailer_url"),
                    movie.get("poster_url"),
                    movie.get("release_date"),
                    movie.get("status")
            );
            return ResponseEntity.ok().build();
        } catch (DataAccessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMovie(@PathVariable Integer id, @RequestBody Map<String, Object> movie) {
        String sql = """
            UPDATE MOVIES
            SET title=?, description=?, director=?, movie_cast=?, duration=?,
                movie_type=?, language=?, trailer_url=?, poster_url=?, release_date=?, status=?
            WHERE id=?
        """;

        try {
            jdbcTemplate.update(sql,
                    movie.get("title"),
                    movie.get("description"),
                    movie.get("director"),
                    movie.get("movie_cast"),
                    movie.get("duration"),
                    movie.get("movie_type"),
                    movie.get("language"),
                    movie.get("trailer_url"),
                    movie.get("poster_url"),
                    movie.get("release_date"),
                    movie.get("status"),
                    id
            );
            return ResponseEntity.ok().build();
        } catch (DataAccessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Integer id) {
        String sql = "DELETE FROM MOVIES WHERE id = ?";
        try {
            jdbcTemplate.update(sql, id);
            return ResponseEntity.ok().build();
        } catch (DataAccessException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
