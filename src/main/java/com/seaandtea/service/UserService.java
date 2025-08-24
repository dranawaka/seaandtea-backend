package com.seaandtea.service;

import com.seaandtea.dto.UserDto;
import com.seaandtea.dto.UserRoleUpdateRequest;
import com.seaandtea.entity.User;
import com.seaandtea.repository.UserRepository;
import com.seaandtea.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserDto(user);
    }
    
    public UserDto updateUserProfile(Long userId, UserDto updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update only the fields that are provided
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getNationality() != null) {
            user.setNationality(updateRequest.getNationality());
        }
        
        User savedUser = userRepository.save(user);
        return mapToUserDto(savedUser);
    }
    
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public UserDto updateUserRole(Long userId, UserRoleUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            User.UserRole newRole = User.UserRole.valueOf(request.getRole().toUpperCase());
            user.setRole(newRole);
            User savedUser = userRepository.save(user);
            return mapToUserDto(savedUser);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + request.getRole());
        }
    }
    
    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .nationality(user.getNationality())
                .profilePictureUrl(user.getProfilePictureUrl())
                .isVerified(user.getIsVerified())
                .isActive(user.getIsActive())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
