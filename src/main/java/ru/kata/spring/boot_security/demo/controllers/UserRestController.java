package ru.kata.spring.boot_security.demo.controllers;


import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.dto.ApiResponseDTO;
import ru.kata.spring.boot_security.demo.dto.UserCreateRequestDTO;
import ru.kata.spring.boot_security.demo.dto.UserResponseDTO;
import ru.kata.spring.boot_security.demo.dto.UserUpdateRequestDTO;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.entities.User;
import ru.kata.spring.boot_security.demo.mapper.UserMapper;
import ru.kata.spring.boot_security.demo.services.RoleService;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserRestController {

    private final UserService userService;
    private final RoleService roleService;
    private final UserMapper userMapper;

    public UserRestController(UserService userService,
                              RoleService roleService,
                              UserMapper userMapper) {
        this.userService = userService;
        this.roleService = roleService;
        this.userMapper = userMapper;
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> getCurrentUser(
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDTO.error("User not authenticated"));
        }

        UserResponseDTO userDTO = userMapper.toResponseDTO(currentUser);
        return ResponseEntity.ok(ApiResponseDTO.success(userDTO));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponseDTO<List<UserResponseDTO>>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserResponseDTO> userDTOs = users.stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDTO.success(userDTOs));
    }

    // ✅ НОВЫЙ МЕТОД - для получения пользователя по ID (нужен для редактирования)
    @GetMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> getUserById(@PathVariable Long id) {
        System.out.println("=== getUserById called, id: " + id);

        Optional<User> userOpt = userService.findById(id);

        if (userOpt.isPresent()) {
            UserResponseDTO userDTO = userMapper.toResponseDTO(userOpt.get());
            return ResponseEntity.ok(ApiResponseDTO.success(userDTO));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("User not found with id: " + id));
        }
    }

    @PostMapping("/admin/users")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> createUser(
            @Valid @RequestBody UserCreateRequestDTO request) {

        if (userService.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponseDTO.error("User with email " + request.getEmail() + " already exists"));
        }

        User user = userMapper.toEntity(request);
        Set<Role> roles = getRolesFromNames(request.getRoleNames());
        user.setRoles(roles);

        User savedUser = userService.save(user);
        UserResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("User created successfully", responseDTO));
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDTO request) {

        Optional<User> userOpt = userService.findById(id);

        if (!userOpt.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("User not found"));
        }

        User existingUser = userOpt.get();

        // Обновляем поля
        updateUserFields(existingUser, request);

        // Обновляем роли, если переданы
        if (request.getRoleNames() != null) {
            Set<Role> roles = getRolesFromNames(request.getRoleNames());
            existingUser.setRoles(roles);
        }

        // Сохраняем (пароль закодируется в сервисе, если изменился)
        User updatedUser = userService.update(existingUser);

        return ResponseEntity.ok(ApiResponseDTO.success("User updated",
                userMapper.toResponseDTO(updatedUser)));
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);

        if (!userOpt.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("User not found"));
        }

        userService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success("User deleted successfully", null));
    }

    @GetMapping("/admin/roles")
    public ResponseEntity<ApiResponseDTO<List<String>>> getAllRoles() {
        List<Role> roles = roleService.findAll();
        List<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDTO.success(roleNames));
    }

    // Вспомогательные методы
    private Set<Role> getRolesFromNames(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleService.findByName(roleName);
            if (role != null) {
                roles.add(role);
            }
        }
        return roles;
    }

    private void updateUserFields(User user, UserUpdateRequestDTO request) {
        if (request.getFirstname() != null) {
            user.setFirstname(request.getFirstname());
        }
        if (request.getSurname() != null) {
            user.setSurname(request.getSurname());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getCandrive() != null) {
            user.setCandrive(request.getCandrive());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(request.getPassword());
        }
    }
}