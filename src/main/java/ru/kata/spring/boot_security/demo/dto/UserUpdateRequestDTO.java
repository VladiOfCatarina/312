package ru.kata.spring.boot_security.demo.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserUpdateRequestDTO {

    @Size(min = 2, max = 50, message = "Firstname must be between 2 and 50 characters")
    private String firstname;

    @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
    private String surname;

    @Past(message = "Birthday must be in the past")
    private LocalDate birthday;

    @Email(message = "Invalid email format")
    private String email;

    private Boolean candrive;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private Set<String> roleNames;
}