package com.fukuoka_fg.solidws.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController の単体テスト")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private static User sampleUser(Long id, String accountId, int score, LocalDateTime lastLogin) {
        User user = new User();
        user.setId(id);
        user.setAccountId(accountId);
        user.setScore(score);
        user.setLastLogin(lastLogin);
        user.setPassword("secret");
        return user;
    }

    @Nested
    @DisplayName("getMyInfo")
    class GetMyInfo {
        @Test
        @DisplayName("ユーザーが存在する場合、GET /users/{id} は200と最小情報を返す")
        void returnsOkAndBodyWhenUserExists() {
            // Arrange
            User user = sampleUser(1L, "alice", 1200, null);
            when(userService.findById(1L)).thenReturn(Optional.of(user));

            // Act
            ResponseEntity<Map<String, Object>> response = userController.getMyInfo(1L);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().get("id"));
            assertEquals("alice", response.getBody().get("accountId"));
            assertEquals(1200, response.getBody().get("score"));
            verify(userService, times(1)).findById(1L);
        }

        @Test
        @DisplayName("ユーザーが存在しない場合、GET /users/{id} は404を返す")
        void returnsNotFoundWhenUserMissing() {
            // Arrange
            when(userService.findById(99L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<Map<String, Object>> response = userController.getMyInfo(99L);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
            verify(userService, times(1)).findById(99L);
        }
    }

    @Nested
    @DisplayName("login")
    class Login {
        @Test
        @DisplayName("認証成功の場合、POST /users/login は200とユーザー情報を返す")
        void returnsOkWhenLoginSuccess() {
            // Arrange
            User user = sampleUser(1L, "alice", 1200, null);
            when(userService.login("alice", "pass")).thenReturn(Optional.of(user));

            // Act
            ResponseEntity<Map<String, Object>> response = userController.login("alice", "pass");

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().get("id"));
            assertEquals("alice", response.getBody().get("accountId"));
            assertEquals(1200, response.getBody().get("score"));
            verify(userService, times(1)).login("alice", "pass");
        }

        @Test
        @DisplayName("認証失敗の場合、POST /users/login は403を返す")
        void returnsForbiddenWhenLoginFails() {
            // Arrange
            when(userService.login("alice", "bad")).thenReturn(Optional.empty());

            // Act
            ResponseEntity<Map<String, Object>> response = userController.login("alice", "bad");

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNull(response.getBody());
            verify(userService, times(1)).login("alice", "bad");
        }
    }

    @Nested
    @DisplayName("listUsers")
    class ListUsers {
        @Test
        @DisplayName("ユーザー一覧がある場合、GET /users は200と一覧を返す")
        void returnsOkAndList() {
            // Arrange
            LocalDateTime lastLogin = LocalDateTime.of(2025, 10, 10, 10, 0);
            List<User> users = List.of(
                    sampleUser(1L, "alice", 1200, lastLogin),
                    sampleUser(2L, "bob", 500, lastLogin)
            );
            when(userService.listUsers()).thenReturn(users);

            // Act
            ResponseEntity<List<Map<String, Object>>> response = userController.listUsers();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            assertEquals(1L, response.getBody().get(0).get("id"));
            assertEquals("alice", response.getBody().get(0).get("accountId"));
            assertEquals(lastLogin, response.getBody().get(0).get("lastLogin"));
            verify(userService, times(1)).listUsers();
        }
    }

    @Nested
    @DisplayName("addUser")
    class AddUser {
        @Test
        @DisplayName("パラメータが正しい場合、POST /users は200を返す")
        void returnsOkWhenAddSuccess() {
            // Arrange
            when(userService.addUser("alice", "pass")).thenReturn(sampleUser(1L, "alice", 0, null));

            // Act
            ResponseEntity<Void> response = userController.addUser("alice", "pass");

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(userService, times(1)).addUser("alice", "pass");
        }

        @Test
        @DisplayName("必須パラメータが不正な場合、POST /users は400を返す")
        void returnsBadRequestWhenAddThrowsIllegalArgument() {
            // Arrange
            doThrow(new IllegalArgumentException("accountId or password is blank"))
                    .when(userService).addUser("", "");

            // Act
            ResponseEntity<Void> response = userController.addUser("", "");

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(userService, times(1)).addUser("", "");
        }

        @Test
        @DisplayName("accountIdが重複している場合、POST /users は409を返す")
        void returnsConflictWhenAddThrowsIllegalState() {
            // Arrange
            doThrow(new IllegalStateException("accountId already exists"))
                    .when(userService).addUser("alice", "pass");

            // Act
            ResponseEntity<Void> response = userController.addUser("alice", "pass");

            // Assert
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            verify(userService, times(1)).addUser("alice", "pass");
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {
        @Test
        @DisplayName("更新対象が存在する場合、PATCH /users/{id} は200を返す")
        void returnsOkWhenUpdateSuccess() {
            // Arrange
            when(userService.updateUser(1L, "alice", "pass"))
                    .thenReturn(Optional.of(sampleUser(1L, "alice", 0, null)));

            // Act
            ResponseEntity<Void> response = userController.updateUser(1L, "alice", "pass");

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(userService, times(1)).updateUser(1L, "alice", "pass");
        }

        @Test
        @DisplayName("更新対象が存在しない場合、PATCH /users/{id} は404を返す")
        void returnsNotFoundWhenUpdateMissing() {
            // Arrange
            when(userService.updateUser(99L, null, null)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<Void> response = userController.updateUser(99L, null, null);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(userService, times(1)).updateUser(99L, null, null);
        }

        @Test
        @DisplayName("パラメータが不正な場合、PATCH /users/{id} は400を返す")
        void returnsBadRequestWhenUpdateThrowsIllegalArgument() {
            // Arrange
            doThrow(new IllegalArgumentException("accountId is blank"))
                    .when(userService).updateUser(1L, "", null);

            // Act
            ResponseEntity<Void> response = userController.updateUser(1L, "", null);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(userService, times(1)).updateUser(1L, "", null);
        }

        @Test
        @DisplayName("accountIdが重複している場合、PATCH /users/{id} は409を返す")
        void returnsConflictWhenUpdateThrowsIllegalState() {
            // Arrange
            doThrow(new IllegalStateException("accountId already exists"))
                    .when(userService).updateUser(1L, "alice", null);

            // Act
            ResponseEntity<Void> response = userController.updateUser(1L, "alice", null);

            // Assert
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            verify(userService, times(1)).updateUser(1L, "alice", null);
        }
    }
}
