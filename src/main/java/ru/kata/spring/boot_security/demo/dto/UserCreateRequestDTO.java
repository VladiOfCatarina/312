package ru.kata.spring.boot_security.demo.dto;


import javax.validation.constraints.*;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserCreateRequestDTO {
    @NotBlank(message = "Firstname is required")
    @Size(min = 2, max = 50, message = "Firstname must be between 2 and 50 characters")
    private String firstname;  // ✅ changed from name

    @NotBlank(message = "Surname is required")
    @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
    private String surname;

    @NotNull(message = "Birthday is required")
    @Past(message = "Birthday must be in the past")
    private LocalDate birthday;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private Boolean candrive;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private Set<String> roleNames;
}
