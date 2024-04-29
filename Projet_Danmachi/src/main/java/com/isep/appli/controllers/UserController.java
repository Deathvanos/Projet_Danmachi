package com.isep.appli.controllers;

import com.isep.appli.models.Persona;
import com.isep.appli.services.PersonaService;
import com.isep.appli.services.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.isep.appli.models.User;
import com.isep.appli.services.UserService;

import jakarta.validation.Valid;

import java.util.List;

@Controller
public class UserController {
	
	private final UserService userService;
	private final EmailService emailService;
	private final PersonaService personaService;

	UserController(UserService userService, EmailService emailService, PersonaService personaService) {
		this.userService = userService;
		this.emailService = emailService;
		this.personaService = personaService;
	}
	
	/*******************************************************************************/
	/******************************** ALL ******************************************/
	/*******************************************************************************/
	@GetMapping("/home")
	public ModelAndView home() {
		ModelAndView modelAndView = new ModelAndView("index");
		return modelAndView;
	}
	
	@GetMapping("/")
	public String index() {
		return "redirect:/home";
	}

	/*******************************************************************************/
	/******************************** GUEST ****************************************/
	/*******************************************************************************/
	
	
	@GetMapping("/subscription") 
	public String subscriptionPage(Model model) {
		model.addAttribute("user", new User());
		return "subscription";
	}
	
	@PostMapping("/subscription") 
		public String subscription(@Valid User user, BindingResult result, Model model) {
		userService.signup(user);

		String confirmationUrl = "http://localhost:8080/confirm?id=" + user.getId();
		String emailContent = "Please click the following link to confirm your email address: " + confirmationUrl;

		emailService.sendEmail(user.getEmail(), emailContent, "Confirmer adresse mail");

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
	
	
	/*******************************************************************************/
	/******************************** PLAYER ***************************************/
	/*******************************************************************************/

	@GetMapping("/user-profile")
	public String checkLogin(Model model, HttpSession session) {

		User user = (User) session.getAttribute("user");
		List<Persona> personas = personaService.getPersonasByUser(user);

		model.addAttribute("user", user);
		model.addAttribute("personas", personas);
		model.addAttribute("persona", new Persona());
		return "user-profile";
	}

	@PostMapping("/save-persona")
	public String savePersonaToUser(@Valid Persona persona,
									@RequestParam("file") MultipartFile file,
									HttpSession session,
									Model model) {
		User user = (User) session.getAttribute("user");

		if (personaService.savePersona(file, user, persona)) {
			return "redirect:/user-profile";
		}

		model.addAttribute("createPersonaError", true);
		return "user-profile";
	}
}
