package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.CreateAdminRequest;
import com.agentsbackend.DTO.response.AdminResponse;
import com.agentsbackend.entities.Admin;
import com.agentsbackend.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
@Tag(name = "Admins", description = "Administrative user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    @Operation(summary = "Create an admin", description = "Creates an administrative user with a securely hashed password.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Admin created"),
        @ApiResponse(responseCode = "400", description = "Invalid admin request"),
        @ApiResponse(responseCode = "409", description = "Admin email already exists")
    })
    public ResponseEntity<AdminResponse> createAdmin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Admin creation details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateAdminRequest.class)))
            @Valid @RequestBody CreateAdminRequest request) {
        Admin createdAdmin = adminService.createAdmin(request);
        return new ResponseEntity<>(AdminResponse.from(createdAdmin), HttpStatus.CREATED);
    }

    /**
     * Compatibility entry point for callers that already provide an Admin entity.
     * The HTTP endpoint above is the public API and accepts only CreateAdminRequest.
     */
    public ResponseEntity<Admin> createAdmin(Admin admin) {
        Admin createdAdmin = adminService.createAdmin(admin);
        return new ResponseEntity<>(createdAdmin, HttpStatus.CREATED);
    }
}
