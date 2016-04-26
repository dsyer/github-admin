package com.example;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebConfiguration {

	@RequestMapping("/login")
	public String login(HttpServletRequest request, Map<String, Object> model) {
		// Could be a view controller in Spring Boot 1.4
		model.put("_csrf", ((CsrfToken)request.getAttribute("_csrf")).getToken());
		return "login";
	}

}
