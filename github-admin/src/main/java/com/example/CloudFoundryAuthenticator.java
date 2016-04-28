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

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.discovery.CloudFoundryDiscoveryProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

/**
 * @author Dave Syer
 *
 */
@Service
public class CloudFoundryAuthenticator {

	private CloudFoundryDiscoveryProperties discovery;
	private MutableCloudCredentials cloudCredentials;

	@Autowired
	public CloudFoundryAuthenticator(CloudFoundryDiscoveryProperties discovery, MutableCloudCredentials cloudCredentials) {
		this.discovery = discovery;
		this.cloudCredentials = cloudCredentials;
	}

	public void authenticate(String username, String password)
			throws AuthenticationException {
		try {
			OAuth2AccessToken access = cloudFoundryClient(new CloudCredentials(username, password)).login();
			this.cloudCredentials.setToken(access);
		}
		catch (Exception e) {
			throw new BadCredentialsException("Cannot authenticate");
		}
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
