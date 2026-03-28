package ru.kata.spring.boot_security.demo.mapper;

import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.dto.UserCreateRequestDTO;
import ru.kata.spring.boot_security.demo.dto.UserResponseDTO;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.entities.User;
import ru.kata.spring.boot_security.demo.services.RoleService;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public final RoleService roleService;

    public UserMapper(RoleService roleService) {
        this.roleService = roleService;
    }

    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) return null;

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFirstname(user.getFirstname());
        dto.setSurname(user.getSurname());
        dto.setBirthday(user.getBirthday());
        dto.setEmail(user.getEmail());
        dto.setCandrive(user.getCandrive());

        if (user.getRoles() != null) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            dto.setRoles(roleNames);
        }
        return dto;
    }


    public User toEntity(UserCreateRequestDTO request) {
        if (request == null) return null;

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setSurname(request.getSurname());
        user.setBirthday(request.getBirthday());
        user.setEmail(request.getEmail());
        user.setCandrive(request.getCandrive() != null ? request.getCandrive() : false);
        user.setPassword(request.getPassword());

        return user;

    }

}

