package com.veeteq.auth.authservice.mapper;

import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.rest.dto.UserRegistrationDto;
import com.veeteq.auth.authservice.rest.dto.UserResponseDto;
import com.veeteq.auth.authservice.rest.dto.UserRoleDto;
import org.springframework.stereotype.Component;

@Component
public class AuthUserMapper {

    public AuthUser toEntity(UserRegistrationDto dto) {
        var entity = new AuthUser();
        entity.setUsername(dto.getUsername())
                .setEmail(dto.getEmail())
                .setFirstname(dto.getFirstName())
                .setLastname(dto.getLastName())
                .setEnabled(true);
        return entity;
    }

    public UserResponseDto toDto(AuthUser entity) {
        var dto = new UserResponseDto();
        dto.id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .firstName(entity.getFirstname())
                .lastName(entity.getLastname())
                .enabled(entity.isEnabled());
        entity.getRoles().forEach(userRole -> dto.addRolesItem(UserRoleDto.valueOf(userRole.name())));
        return dto;
    }
}
