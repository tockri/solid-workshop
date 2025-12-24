package com.fukuoka_fg.solidws.user;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Data
@Table("USERS")
public class User {
    @Id
    private Long id;
    private String accountId;
    private LocalDateTime lastLogin;
    private Integer score;
    private String password;
}
