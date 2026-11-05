package com.seaandtea.service;

import com.seaandtea.dto.AuthResponse;
import com.seaandtea.dto.LoginRequest;
import com.seaandtea.dto.RegisterRequest;
import com.seaandtea.entity.User;
import com.seaandtea.repository.UserRepository;
import com.seaandtea.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .nationality(request.getNationality())
                .role(User.UserRole.USER)
                .build();
        
        User savedUser = userRepository.save(user);
        
        String jwtToken = jwtService.generateTokenWithUserId(savedUser, savedUser.getId());
        
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(jwtToken) // In production, implement proper refresh token
                .expiresIn(86400000L) // 24 hours
                .user(mapToUserDto(savedUser))
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String jwtToken = jwtService.generateTokenWithUserId(user, user.getId());
        
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(jwtToken) // In production, implement proper refresh token
                .expiresIn(86400000L) // 24 hours
                .user(mapToUserDto(user))
                .build();
    }
    
    private com.seaandtea.dto.UserDto mapToUserDto(User user) {
        return com.seaandtea.dto.UserDto.builder()
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

