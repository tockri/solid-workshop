package com.fukuoka_fg.solidws.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // (player) GET /users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMyInfo(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = userOpt.get();
        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("accountId", user.getAccountId());
        body.put("score", user.getScore());
        return ResponseEntity.ok(body);
    }

    // (player) POST /users/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam("accountId") String accountId,
            @RequestParam("password") String password) {
        Optional<User> userOpt = userService.login(accountId, password);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        User user = userOpt.get();
        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("accountId", user.getAccountId());
        body.put("score", user.getScore());
        return ResponseEntity.ok(body);
    }

    // (admin) GET /users
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        List<User> users = userService.listUsers();
        List<Map<String, Object>> body = users.stream().map(user -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", user.getId());
            row.put("accountId", user.getAccountId());
            row.put("lastLogin", user.getLastLogin());
            return row;
        }).toList();
        return ResponseEntity.ok(body);
    }

    // (admin) POST /users
    @PostMapping
    public ResponseEntity<Void> addUser(
            @RequestParam("accountId") String accountId,
            @RequestParam("password") String password) {
        try {
            userService.addUser(accountId, password);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // (admin) PATCH /users/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateUser(
            @PathVariable Long id,
            @RequestParam(value = "accountId", required = false) String accountId,
            @RequestParam(value = "password", required = false) String password) {
        try {
            Optional<User> updated = userService.updateUser(id, accountId, password);
            if (updated.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
