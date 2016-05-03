package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

	private CloudFoundryAuthenticator authenticator;

	@Autowired
	public AuthController(CloudFoundryAuthenticator authenticator) {
		this.authenticator = authenticator;
	}

	@RequestMapping(value = "/auth", method = RequestMethod.GET)
	public String login() {
		return "forward:login.html";
	}

	@RequestMapping(value = "/auth", method = RequestMethod.POST)
	@ResponseBody
	public String authenticate(@RequestBody Credentials credentials) {
		this.authenticator.authenticate(credentials.username, credentials.password);
		return "[\"OK\"]";
	}

	@ExceptionHandler(BadCredentialsException.class)
	public String error() {
		return "login";
	}

}

class Credentials {
	public String username;
	public String password;
}
