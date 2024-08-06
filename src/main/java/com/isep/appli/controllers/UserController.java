package com.isep.appli.controllers;

import com.isep.appli.dbModels.Message;
import com.isep.appli.dbModels.Personnage;
import com.isep.appli.dbModels.User;
import com.isep.appli.models.*;
import com.isep.appli.services.EmailService;
import com.isep.appli.services.PersonnageService;
import com.isep.appli.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {
	@Autowired
	private PersonnageService personnageService;
	@Autowired
	private UserService userService;
	@Autowired
	private EmailService emailService;


	static public String checkIsUser(User user, Model model) {
		if (user==null) {return "errors/error-401";}
		model.addAttribute("user", user);
		return "200";
	}

	@GetMapping("/users")
	public ResponseEntity<List<User>> findConnectedUsers() {
		return ResponseEntity.ok(userService.findConnectedUsers());
	}

	@GetMapping("/user-profile")
	public String checkLogin(Model model, HttpSession session) {

		User user = (User) session.getAttribute("user");
		checkIsUser(user, model);

		List<Personnage> personnages = personnageService.getPersonasByUser(user);
		if(!personnages.isEmpty()){
			session.setAttribute("personnage", personnages.get(0));
		}

		model.addAttribute("user", user);
		model.addAttribute("personnages", personnages);
		model.addAttribute("newPersonnage", new Personnage());
		return "user-profile";
	}

	@PostMapping("/modify-user")
	public String updateUser(@ModelAttribute ModifyUserInfoForm newUserInfo, HttpSession session) {
		User user = (User) session.getAttribute("user");
		Map<String, String> checkUniqueParameters = new HashMap<>();

		if (!newUserInfo.getUsername().isEmpty()) {
			checkUniqueParameters.put("username", newUserInfo.getUsername());
		}
		if (!newUserInfo.getEmail().isEmpty()) {
			checkUniqueParameters.put("email", newUserInfo.getEmail());
		}

		userService.modifyUserInfo(user, newUserInfo);

		if (newUserInfo.getEmail() != null && !newUserInfo.getEmail().isEmpty()) {
			emailService.sendConfirmationEmail(user);
		}

		return "redirect:/user-profile";
	}



}
