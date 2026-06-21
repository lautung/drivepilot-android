package com.lautung.phonecar.backend.user;

import com.lautung.phonecar.backend.common.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class CurrentUserController {
    private final UserRepository users;

    public CurrentUserController(UserRepository users) { this.users = users; }

    @GetMapping
    CurrentUserResponse me(@AuthenticationPrincipal Jwt jwt) {
        UserEntity user = users.findById(UUID.fromString(jwt.getSubject()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "User no longer exists"));
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.getRole());
    }

    public record CurrentUserResponse(UUID id, String username, UserRole role) {}
}
