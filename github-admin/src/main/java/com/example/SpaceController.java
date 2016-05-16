package com.example;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;

import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.discovery.CloudFoundryDiscoveryProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpaceController {

	private SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
	private CloudFoundryDiscoveryProperties discovery;
	private SpaceService service;

	@Autowired
	public SpaceController(CloudFoundryDiscoveryProperties discovery, SpaceService service, RequestCache requestCache) {
		this.discovery = discovery;
		this.service = service;
		handler.setRequestCache(requestCache);
	}

	@RequestMapping(value = "/auth", method = RequestMethod.GET)
	public String login() {
		return "forward:choose.html";
	}

	@RequestMapping(value = "/spaces", method = RequestMethod.GET)
	@ResponseBody
	public Collection<CloudSpace> spaces(@RequestParam(required = false) String error) {
		return service.spaces();
	}

	@RequestMapping(value = "/spaces", method = RequestMethod.POST)
	@ResponseBody
	public String authenticate(@RequestBody Credentials credentials,
			Principal principal) {
		this.service.choose(credentials.org, credentials.space);
		discovery.setOrg(credentials.org);
		discovery.setSpace(credentials.space);
		Authentication authentication = (Authentication) principal;
		Collection<GrantedAuthority> authorities = new ArrayList<>(
				authentication.getAuthorities());
		authorities.add(new SimpleGrantedAuthority("ROLE_SPACE"));
		PreAuthenticatedAuthenticationToken successful = new PreAuthenticatedAuthenticationToken(
				authentication.getPrincipal(), authentication.getCredentials(),
				authorities);
		successful.setDetails(authentication.getDetails());
		SecurityContextHolder.getContext().setAuthentication(successful);
		return "[\"OK\"]";
	}

	@ExceptionHandler(BadCredentialsException.class)
	public String error() {
		return "login";
	}

}

class Credentials {
	public String org;
	public String space;
}
