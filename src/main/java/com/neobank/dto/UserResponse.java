package com.neobank.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.neobank.entity.User;
import com.neobank.enums.UserRole;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        UserRole userRole,
        boolean active,
        LocalDateTime createdAt

) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt());
    }
}
