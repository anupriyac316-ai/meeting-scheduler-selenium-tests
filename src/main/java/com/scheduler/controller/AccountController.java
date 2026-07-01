package com.scheduler.controller;

import com.scheduler.model.User;
import com.scheduler.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/account/delete")
    public String deleteAccount(Authentication auth,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        User user = userService.findByEmail(auth.getName()).orElseThrow();

        userService.deleteUser(user);

        // Invalidate session now that the account no longer exists
        new SecurityContextLogoutHandler().logout(request, response, auth);
        SecurityContextHolder.clearContext();

        return "redirect:/home?accountDeleted";
    }
}