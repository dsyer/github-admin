package com.example;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

	private CloudFoundryAuthenticator authenticator;

	public AuthController(CloudFoundryAuthenticator authenticator) {
		this.authenticator = authenticator;
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@PostMapping("/login")
	public String authenticate(@RequestParam String username, @RequestParam String password) {
		this.authenticator.authenticate(username, password);
		return "redirect:/";
	}

	@ExceptionHandler(BadCredentialsException.class)
	public String error() {
		return "login";
	}

}
