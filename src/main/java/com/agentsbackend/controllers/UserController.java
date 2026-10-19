
package com.agentsbackend.controllers;

import com.agentsbackend.services.AuthService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public AuthModels.UserResponse currentUser(
            @AuthenticationPrincipal Jwt jwt) {

        UUID clientId = UUID.fromString(jwt.getSubject());

        return authService.currentUser(clientId);
    }
}
