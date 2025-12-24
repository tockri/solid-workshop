package com.fukuoka_fg.solidws.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Optional<User> login(String accountId, String password) {
        if (accountId == null || password == null) {
            return Optional.empty();
        }
        Optional<User> userOpt = userRepository.findByAccountId(accountId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        if (!password.equals(user.getPassword())) {
            return Optional.empty();
        }
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        return Optional.of(user);
    }

    public List<User> listUsers() {
        List<User> users = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            users.add(user);
        }
        return users;
    }

    @Transactional
    public User addUser(String accountId, String password) {
        if (accountId == null || accountId.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("accountId or password is blank");
        }
        if (userRepository.existsByAccountId(accountId)) {
            throw new IllegalStateException("accountId already exists");
        }
        User user = new User();
        user.setAccountId(accountId);
        user.setPassword(password);
        user.setScore(0);
        user.setLastLogin(null);
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> updateUser(Long id, String accountId, String password) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        if (accountId != null) {
            if (accountId.isBlank()) {
                throw new IllegalArgumentException("accountId is blank");
            }
            if (!accountId.equals(user.getAccountId()) && userRepository.existsByAccountId(accountId)) {
                throw new IllegalStateException("accountId already exists");
            }
            user.setAccountId(accountId);
        }
        if (password != null) {
            if (password.isBlank()) {
                throw new IllegalArgumentException("password is blank");
            }
            user.setPassword(password);
        }
        userRepository.save(user);
        return Optional.of(user);
    }
}
