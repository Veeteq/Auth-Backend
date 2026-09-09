package com.veeteq.auth.authservice.rest.api;

import com.veeteq.auth.authservice.entity.UserRole;
import com.veeteq.auth.authservice.mapper.AuthUserMapper;
import com.veeteq.auth.authservice.rest.dto.UserResponseDto;
import com.veeteq.auth.authservice.rest.dto.UserRolesDto;
import com.veeteq.auth.authservice.rest.dto.UserUpdateDto;
import com.veeteq.auth.authservice.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "${app.api.base-path}")
@PreAuthorize("hasAuthority('ACCOUNT_ADMIN')")
public class AdminController implements AdminApi {
    private final static Logger LOGGER = LoggerFactory.getLogger(AdminController.class.getSimpleName());

    private final AdminService adminService;
    private final AuthUserMapper authUserMapper;

    public AdminController(AdminService adminService, AuthUserMapper authUserMapper) {
        this.adminService = adminService;
        this.authUserMapper = authUserMapper;
    }

    @Override
    public ResponseEntity<List<UserResponseDto>> getUsers() {
        LOGGER.info("Request received to list all users");

        var response = adminService.getUsers()
                .stream()
                .map(authUserMapper::toDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable(name = "id", required = true) Long id) {
        LOGGER.info("Request received retrieve single user by id:{}", id);

        var saved = adminService.findById(id);
        var response = authUserMapper.toDto(saved);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable(name = "id", required = true) Long id, @RequestBody UserUpdateDto userUpdateDto) {
        LOGGER.info("Request received to update user data");

        var updatedUser = adminService.update(id, userUpdateDto.getEmail(), userUpdateDto.getFirstName(), userUpdateDto.getLastName(), userUpdateDto.getEnabled());
        var response = authUserMapper.toDto(updatedUser);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "id", required = true) Long id) {
        LOGGER.info("Request received to delete user");
        adminService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/users/{id}/enable")
    public ResponseEntity<UserResponseDto> enableUser(@PathVariable(name = "id", required = true) Long id) {

        var saved = adminService.enable(id);
        var response = authUserMapper.toDto(saved);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/admin/users/{id}/disable")
    public ResponseEntity<UserResponseDto> disableUser(@PathVariable(name = "id", required = true) Long id) {

        var saved = adminService.disable(id);
        var response = authUserMapper.toDto(saved);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserResponseDto> assignRolesToUser(@PathVariable(name = "id", required = true) Long id, @RequestBody UserRolesDto request) {

        var roles = request.getRoles().stream()
                .map(role -> UserRole.valueOf(role.name()))
                .collect(Collectors.toSet());
        var user = adminService.updateRoles(id, roles);
        var response = authUserMapper.toDto(user);
        return ResponseEntity.ok(response);
    }

}

