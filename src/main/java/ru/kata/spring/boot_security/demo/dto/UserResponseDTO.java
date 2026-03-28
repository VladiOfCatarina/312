package ru.kata.spring.boot_security.demo.dto;

import lombok.Data;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.entities.User;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserResponseDTO {
    private Long id;
    private String firstname;
    private String surname;
    private LocalDate birthday;
    private String email;
    private Boolean candrive;
    private Set<String> roles;

    public UserResponseDTO() {
    }
}
