package com.example.taskmanagementsystem.dto.user;

import com.example.taskmanagementsystem.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserResponseConverter {
    public UserResponse convertUserToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail()).build();
    }
}
