/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.discovery.CloudFoundryDiscoveryProperties;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

/**
 * @author Dave Syer
 *
 */
@Service
public class CloudFoundryAuthenticationProvider implements AuthenticationProvider {

	private CloudFoundryDiscoveryProperties discovery;

	@Autowired
	public CloudFoundryAuthenticationProvider(CloudFoundryDiscoveryProperties discovery) {
		this.discovery = discovery;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
		String username = token.getName();
		String password = token.getCredentials().toString();
		try {
			OAuth2AccessToken access = cloudFoundryClient(cloudCredentials(username, password)).login();
			UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(username, password,
					AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
			Map<String,Object> map = new HashMap<>();
			map.put("username", username);
			map.put("password", password);
			map.put("token", access);
			result.setDetails(map);
			return result;
		}
		catch (Exception e) {
			throw new BadCredentialsException("Cannot authenticate");
		}
	}

	private CloudCredentials cloudCredentials(String username, String password) {
		return new CloudCredentials(username, password);
	}

	private CloudFoundryClient cloudFoundryClient(CloudCredentials cc)
			throws MalformedURLException {
		CloudFoundryClient cloudFoundryClient;
		if (this.discovery.getOrg() != null && this.discovery.getSpace() != null) {
			cloudFoundryClient = new CloudFoundryClient(cc,
					URI.create(this.discovery.getUrl()).toURL(), this.discovery.getOrg(),
					this.discovery.getSpace());
		}
		else {
			cloudFoundryClient = new CloudFoundryClient(cc,
					URI.create(this.discovery.getUrl()).toURL());
		}
		return cloudFoundryClient;
	}

}
