package com.scheduler.controller;

import com.scheduler.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    @GetMapping("/")
public String root() {
    return "redirect:/home";
}

@GetMapping("/home")
public String homePage() {
    return "home";
}

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {
        if (userService.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already registered!");
            return "register";
        }
        userService.register(name, email, password);
        return "redirect:/login?registered";
    }
}