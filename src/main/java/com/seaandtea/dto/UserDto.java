package com.seaandtea.dto;

import com.seaandtea.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate dateOfBirth;
    private String nationality;
    private String profilePictureUrl;
    private Boolean isVerified;
    private Boolean isActive;
    private User.UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

