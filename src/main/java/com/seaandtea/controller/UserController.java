package com.seaandtea.controller;

import com.seaandtea.dto.UserDto;
import com.seaandtea.dto.UserRoleUpdateRequest;
import com.seaandtea.entity.User;
import com.seaandtea.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile management APIs")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Retrieves the authenticated user's profile")
    public ResponseEntity<UserDto> getCurrentUserProfile(@AuthenticationPrincipal User user) {
        UserDto userProfile = userService.getUserProfile(user.getId());
        return ResponseEntity.ok(userProfile);
    }
    
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Updates the authenticated user's profile")
    public ResponseEntity<UserDto> updateUserProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UserDto updateRequest) {
        UserDto updatedProfile = userService.updateUserProfile(user.getId(), updateRequest);
        return ResponseEntity.ok(updatedProfile);
    }
    
    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Changes the authenticated user's password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal User user,
            @RequestBody PasswordChangeRequest request) {
        userService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password changed successfully");
    }
    
    @PutMapping("/role")
    @Operation(summary = "Update user role", description = "Updates the authenticated user's role")
    public ResponseEntity<UserDto> updateUserRole(
            @AuthenticationPrincipal User user,
            @RequestBody UserRoleUpdateRequest request) {
        UserDto updatedUser = userService.updateUserRole(user.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/profile-picture")
    @Operation(summary = "Update profile picture URL", description = "Updates the authenticated user's profile picture URL")
    public ResponseEntity<UserDto> updateProfilePicture(
            @AuthenticationPrincipal User user,
            @RequestBody ProfilePictureUpdateRequest request) {
        UserDto updatedUser = userService.updateProfilePicture(user.getId(), request.getProfilePictureUrl());
        return ResponseEntity.ok(updatedUser);
    }
    
    public static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;
        
        // Getters and setters
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    public static class ProfilePictureUpdateRequest {
        private String profilePictureUrl;
        
        // Getters and setters
        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    }
}
