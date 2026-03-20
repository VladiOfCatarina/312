package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.entities.User;
import ru.kata.spring.boot_security.demo.services.RoleService;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
public class MainController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public MainController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/")
    public String homePage() {
        return "redirect:/user";
    }

    @GetMapping("/user")
    public String userPage(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("user", currentUser);
        return "user";
    }

    @GetMapping("/admin")
    public String adminPage(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("user", currentUser);
        return "admin";
    }

    @GetMapping("/admin/add")
    public String addForm(Model model) {
        model.addAttribute("user", new User());
        return "addform";
    }

    @PostMapping("/admin/add")
    public String create(@RequestParam("name") String name,
                         @RequestParam("surname") String surname,
                         @RequestParam("birthday") String birthday,
                         @RequestParam("email") String email,
                         @RequestParam(value = "candrive", required = false) Boolean candrive,
                         @RequestParam("password") String password,
                         @RequestParam(value = "roleNames", required = false) String[] roleNames) {  // Изменено с roles на roleNames

        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setBirthday(LocalDate.parse(birthday));
        user.setEmail(email);
        user.setCandrive(candrive != null ? candrive : false);
        user.setPassword(password);

        Set<Role> roleSet = new HashSet<>();
        if (roleNames != null) {
            for (String roleName : roleNames) {
                Role role = roleService.findByName(roleName);
                roleSet.add(role);
            }
        }
        user.setRoles(roleSet);

        userService.save(user);

        return "redirect:/admin";
    }

    @GetMapping("/admin/edit")
    public String editUser(@RequestParam(required = false) Long id,
                           @AuthenticationPrincipal User currentUser,
                           Model model) {
        System.out.println("=== editUser called with id: " + id + " ===");

        // Добавляем текущего пользователя для отображения в навбаре
        model.addAttribute("currentUser", currentUser);

        if (id != null) {
            Optional<User> tempUser = userService.findById(id);
            if (tempUser.isPresent()) {
                System.out.println("User found: " + tempUser.get().getName());
                model.addAttribute("user", tempUser.get());
            } else {
                System.out.println("User NOT found with id: " + id);
            }
        }

        // Добавляем список всех пользователей для таблицы справа
        model.addAttribute("users", userService.findAll());

        return "editform";
    }

    @PostMapping("/admin/edit")
    public String update(@ModelAttribute User user,
                         @RequestParam(value = "roleNames", required = false) String[] roleNames,  // Изменено с roles на roleNames
                         @RequestParam(value = "password", required = false) String password) {

        System.out.println("========== UPDATE METHOD CALLED ==========");
        System.out.println("User ID: " + user.getId());

        Optional<User> existingUserOpt = userService.findById(user.getId());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Обновляем поля
            existingUser.setName(user.getName());
            existingUser.setSurname(user.getSurname());
            existingUser.setBirthday(user.getBirthday());
            existingUser.setEmail(user.getEmail());
            existingUser.setCandrive(user.getCandrive());

            // Обновляем пароль только если он был введен
            if (password != null && !password.isEmpty()) {
                existingUser.setPassword(password);
            }

            // Обрабатываем роли
            Set<Role> roleSet = new HashSet<>();
            if (roleNames != null) {
                for (String roleName : roleNames) {
                    Role role = roleService.findByName(roleName);
                    roleSet.add(role);
                }
            }
            existingUser.setRoles(roleSet);

            userService.update(existingUser);
        }

        return "redirect:/admin";
    }

    @GetMapping("/admin/delete")
    public String showDeleteForm() {
        return "deleteuser";
    }

    @PostMapping("/admin/delete")
    public String deleteUser(@RequestParam("id") Long id) {
        userService.delete(id);
        return "redirect:/admin";
    }

    @GetMapping("/admin/getuserinfo")
    public String showUserInfoForm(Model model) {
        // Просто показываем форму, без пользователя
        model.addAttribute("user", null);
        return "userinfo"; // Имя вашего HTML файла
    }

    @PostMapping("/admin/getuserinfo")
    public String getUserInfo(@RequestParam("id") Long id,
                              @AuthenticationPrincipal User currentUser,
                              Model model) {

        Optional<User> userOpt = userService.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("error", null);
        } else {
            model.addAttribute("user", null);
            model.addAttribute("error", "User with ID " + id + " not found!");
        }

        return "userinfo";
    }

}