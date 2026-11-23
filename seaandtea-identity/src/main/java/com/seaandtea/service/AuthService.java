package com.seaandtea.service;

import com.seaandtea.dto.AuthResponse;
import com.seaandtea.dto.LoginRequest;
import com.seaandtea.dto.RegisterRequest;
import com.seaandtea.entity.User;
import com.seaandtea.oauth.OAuthIdentity;
import com.seaandtea.oauth.OAuthLoginException;
import com.seaandtea.repository.UserRepository;
import com.seaandtea.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
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
        return tokensFor(savedUser);
    }

    public AuthResponse loginWithOAuth(OAuthIdentity identity) {
        if (!identity.emailVerified()) {
            throw new OAuthLoginException("The provider did not confirm this email address.");
        }
        User user = userRepository
                .findByAuthProviderAndProviderSubject(identity.storedProvider(), identity.subject())
                .orElse(null);
        if (user == null) {
            user = userRepository.findByEmailIgnoreCase(identity.email()).orElse(null);
            if (user != null && Boolean.FALSE.equals(user.getIsActive())) {
                throw new OAuthLoginException("This account is disabled.");
            }
            if (user == null) {
                user = userRepository.save(newSocialUser(identity));
            } else {
                linkSocialAccount(user, identity);
                user = userRepository.save(user);
            }
        } else if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new OAuthLoginException("This account is disabled.");
        }
        if (identity.emailVerified() && !Boolean.TRUE.equals(user.getIsVerified())) {
            user.setIsVerified(true);
            user = userRepository.save(user);
        }
        return tokensFor(user);
    }
    
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return tokensFor(user);
    }

    private AuthResponse tokensFor(User user) {
        String jwtToken = jwtService.generateTokenWithUserId(user, user.getId());
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(jwtToken) // In production, implement proper refresh token
                .expiresIn(86400000L) // 24 hours
                .user(mapToUserDto(user))
                .build();
    }

    private User newSocialUser(OAuthIdentity identity) {
        return User.builder()
                .email(identity.email())
                .password(passwordEncoder.encode(randomPassword()))
                .firstName(identity.firstName())
                .lastName(identity.lastName())
                .profilePictureUrl(identity.pictureUrl())
                .isVerified(true)
                .isActive(true)
                .role(User.UserRole.USER)
                .authProvider(identity.storedProvider())
                .providerSubject(identity.subject())
                .build();
    }

    private void linkSocialAccount(User user, OAuthIdentity identity) {
        if (user.getProviderSubject() == null || user.getProviderSubject().isBlank()) {
            user.setAuthProvider(identity.storedProvider());
            user.setProviderSubject(identity.subject());
        }
        if ((user.getProfilePictureUrl() == null || user.getProfilePictureUrl().isBlank())
                && identity.pictureUrl() != null) {
            user.setProfilePictureUrl(identity.pictureUrl());
        }
        if (identity.emailVerified()) {
            user.setIsVerified(true);
        }
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
    }

    private static String randomPassword() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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

