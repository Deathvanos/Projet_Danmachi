package com.isep.appli.controllers;

import com.isep.appli.dbModels.User;
import com.isep.appli.services.EmailService;
import com.isep.appli.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.isep.appli.controllers.UserController.checkIsUser;

@Controller
public class LoginController {

    private final UserService userService;
    private final EmailService emailService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
    public ResponseEntity<Map<String, Boolean>> checkUnique(@RequestBody Map<String, String> userInfo) {
        return ResponseEntity.ok(userService.checkUnique(userInfo));
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @MessageMapping("/user.connected")
    @SendTo("/user/topic")
    public User connectedUser(@Payload User user){
        return user;
    }

    @MessageMapping("/user.disconnected")
    @SendTo("/user/topic")
    public User disconnect(@Payload User user){
        userService.logout(user);
        return user;
    }

    @PostMapping("/login")
    public String checkLogin(@Valid @Payload User user, BindingResult result, Model model, HttpSession session) {
        User userSignedIn = userService.login(user.getEmail(), user.getPassword());

        if (userSignedIn == null) {
            model.addAttribute("loginError", true);
            return "login";
        }
        connectedUser(userSignedIn);
        session.setAttribute("user", userSignedIn);
        // Définir un délai pour déconnecter l'utilisateur automatiquement.
        scheduler.schedule(() -> userService.logout(user), 30, TimeUnit.MINUTES);


        if (userSignedIn.getIsAdmin()) {
            return "redirect:/admin/home";
        }

        return "redirect:/user-profile";
    }

    @GetMapping("/logout")
    public String logout(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        String checkUser = checkIsUser(user, model);
        if (!checkUser.equals("200")){return checkUser;}
        disconnect(user);
        session.invalidate();
        return "redirect:/home";
    }
}
