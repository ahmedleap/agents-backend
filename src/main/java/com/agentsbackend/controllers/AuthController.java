package com.agentsbackend.controllers;

import com.agentsbackend.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthModels.MessageResponse> register(@RequestBody AuthModels.RegisterRequest request,
                                                                HttpServletRequest servletRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request,
                servletRequest.getRemoteAddr(), servletRequest.getHeader("User-Agent")));
    }

    @PostMapping("/login")
    public AuthModels.TokenResponse login(@RequestBody AuthModels.LoginRequest request,
                                          HttpServletRequest servletRequest) {
        return authService.login(request, servletRequest.getHeader("User-Agent"));
    }

    @PostMapping("/refresh")
    public AuthModels.TokenResponse refresh(@RequestBody AuthModels.RefreshRequest request) {
        return authService.refresh(request == null ? null : request.refreshToken());
    }

    @PostMapping("/logout")
    public AuthModels.MessageResponse logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(bearerToken(authorization));
        return new AuthModels.MessageResponse("Signed out.");
    }

    @PostMapping("/logout-all")
    public AuthModels.MessageResponse logoutAll(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logoutAll(authService.authenticate(bearerToken(authorization)));
        return new AuthModels.MessageResponse("All sessions signed out.");
    }

    @GetMapping("/sessions")
    public List<AuthModels.SessionResponse> sessions(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return authService.sessions(authService.authenticate(bearerToken(authorization)));
    }
    private String bearerToken(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) return null;
        return authorization.substring(7).trim();
    }
}
