package com.seaandtea.controller;

import com.seaandtea.dto.AdminUserResponse;
import com.seaandtea.entity.User;
import com.seaandtea.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin User Management", description = "Admin APIs to view, ban, unban and remove users (including guides)")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "List users", description = "Paginated list of users with optional role and active filters. Includes guide summary when user is a guide.")
    public ResponseEntity<Page<AdminUserResponse>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20) Pageable pageable) {
        User.UserRole roleEnum = null;
        if (role != null && !role.isBlank()) {
            try {
                roleEnum = User.UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // keep null = no filter
            }
        }
        Page<AdminUserResponse> page = adminUserService.listUsers(roleEnum, isActive, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get a single user with guide summary if they are a guide.")
    public ResponseEntity<AdminUserResponse> getUser(@PathVariable Long id) {
        AdminUserResponse response = adminUserService.getUser(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/ban")
    @Operation(summary = "Ban user", description = "Deactivate user account (they cannot log in). Cannot ban admin users.")
    public ResponseEntity<AdminUserResponse> banUser(@PathVariable Long id) {
        AdminUserResponse response = adminUserService.banUser(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/unban")
    @Operation(summary = "Unban user", description = "Reactivate user account.")
    public ResponseEntity<AdminUserResponse> unbanUser(@PathVariable Long id) {
        AdminUserResponse response = adminUserService.unbanUser(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove user", description = "Permanently delete user and all related data (guide profile, tours, bookings, reviews, messages). Cannot remove admin users.")
    public ResponseEntity<Void> removeUser(@PathVariable Long id) {
        adminUserService.removeUser(id);
        return ResponseEntity.noContent().build();
    }
}
