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

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeployerProperties;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.stereotype.Service;

/**
 * @author Dave Syer
 *
 */
@Service
public class CloudFoundryAuthenticationProvider implements AuthenticationProvider {

	private CloudFoundryDeployerProperties properties;
	private SpringCloudFoundryClient cloudFoundryClient;

	@Autowired
	public CloudFoundryAuthenticationProvider(CloudFoundryDeployerProperties discovery,
			CloudFoundryClient cloudFoundryClient) {
		this.properties = discovery;
		this.cloudFoundryClient = (SpringCloudFoundryClient) cloudFoundryClient;
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
			String access = cloudFoundryClient(username, password).getAccessToken().get();
			UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
					username, password,
					AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
			this.cloudFoundryClient.getConnectionContext().getClientContext()
					.setAccessToken(new DefaultOAuth2AccessToken(access));
			return result;
		}
		catch (Exception e) {
			throw new BadCredentialsException("Cannot authenticate");
		}
	}

	private SpringCloudFoundryClient cloudFoundryClient(String username, String password)
			throws MalformedURLException {
		return SpringCloudFoundryClient.builder().username(username).password(password)
				.host(this.properties.getApiEndpoint().getHost())
				.port(this.properties.getApiEndpoint().getPort()).build();
	}

}
