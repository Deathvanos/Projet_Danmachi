package com.isep.appli.controllers;

import com.isep.appli.models.User;
import com.isep.appli.services.EmailService;
import com.isep.appli.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UserService userService;
    private final EmailService emailService;

    LoginController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }
    @GetMapping("/subscription")
    public String subscriptionPage(Model model) {
        model.addAttribute("user", new User());
        return "subscription";
    }

    @PostMapping("/subscription")
    public String subscription(@Valid User user, BindingResult result, Model model) {
        userService.signup(user);

        emailService.sendConfirmationEmail(user);

        return "redirect:/login";
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam("id") long id) {
        userService.confirmEmail(id);
        return "redirect:/login";
    }

    @PostMapping("/checkUnique")
    public ResponseEntity<Boolean> checkUnique(@RequestBody String email) {
        return ResponseEntity.ok(userService.checkUnique(email));
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String checkLogin(@Valid User user, BindingResult result, Model model, HttpSession session) {
        User userSignedIn = userService.login(user.getEmail(), user.getPassword());

        if (userSignedIn == null) {
            model.addAttribute("loginError", true);
            return "/login";
        }
        session.setAttribute("user", userSignedIn);

        return "redirect:/user-profile";
    }
}
