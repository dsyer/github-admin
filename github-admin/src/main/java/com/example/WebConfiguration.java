package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebConfiguration {

	private CloudFoundryAuthenticator authenticator;

	@Autowired
	public WebConfiguration(CloudFoundryAuthenticator authenticator) {
		this.authenticator = authenticator;
	}

	@RequestMapping(value="/auth", method=RequestMethod.GET)
	public String login() {
		return "forward:login.html";
	}

	@RequestMapping(value="/auth", method=RequestMethod.POST)
	public String authenticate(@RequestParam String username, @RequestParam String password) {
		this.authenticator.authenticate(username, password);
		return "redirect:/";
	}

	@ExceptionHandler(BadCredentialsException.class)
	public String error() {
		return "login";
	}

}
