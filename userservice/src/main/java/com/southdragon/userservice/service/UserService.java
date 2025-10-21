package com.southdragon.userservice.service;

import com.southdragon.userservice.dto.RegisterRequest;
import com.southdragon.userservice.dto.UserResponse;
import com.southdragon.userservice.model.User;
import com.southdragon.userservice.repository.UserRepository;
import jdk.jshell.spi.ExecutionControl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId).orElseThrow( () -> new RuntimeException("User not found!"));
          return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())){
            User existingUser = userRepository.findByEmail(request.getEmail());
             UserResponse.builder()
                    .id(existingUser.getId())
                    .keycloakId(existingUser.getKeycloakId())
                    .email(existingUser.getEmail())
                    .password(existingUser.getPassword())
                    .firstName(existingUser.getFirstName())
                    .lastName(existingUser.getLastName())
                    .createdAt(existingUser.getCreatedAt())
                    .updatedAt(existingUser.getUpdatedAt())
                    .build();
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstname());
        user.setLastName(request.getLastname());
        user.setKeycloakId(request.getKeycloakId());

        User savedUser = userRepository.save(user);
        return UserResponse.builder()
                .id(savedUser.getId())
                .keycloakId(savedUser.getKeycloakId())
                .email(savedUser.getEmail())
                .password(savedUser.getPassword())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }

    public Boolean existByUserId(String userId) {
        log.info("Calling User Validation API for userId: {}", userId);
        return userRepository.existsByKeycloakId(userId);
    }
}
