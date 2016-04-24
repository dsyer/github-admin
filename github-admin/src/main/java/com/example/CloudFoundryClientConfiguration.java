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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * @author Dave Syer
 *
 */
@Configuration
public class CloudFoundryClientConfiguration {

	@Autowired
	private CloudFoundryDiscoveryProperties discovery;

	@Bean
	public MutableCloudCredentials cloudCredentials() {
		return new MutableCloudCredentials();
	}

	@Bean
	@Scope(proxyMode=ScopedProxyMode.TARGET_CLASS)
	@Lazy
	public CloudFoundryClient cloudFoundryClient(CloudCredentials credentials)
			throws MalformedURLException {
		CloudFoundryClient cloudFoundryClient;
		if (this.discovery.getOrg() != null && this.discovery.getSpace() != null) {
			cloudFoundryClient = new CloudFoundryClient(credentials,
					URI.create(this.discovery.getUrl()).toURL(), this.discovery.getOrg(),
					this.discovery.getSpace());
		}
		else {
			cloudFoundryClient = new CloudFoundryClient(credentials,
					URI.create(this.discovery.getUrl()).toURL());
		}
		return cloudFoundryClient;
	}

}

class MutableCloudCredentials extends CloudCredentials {
	private OAuth2AccessToken token;
	public MutableCloudCredentials() {
		super("","");
	}
	@Override
	public OAuth2AccessToken getToken() {
		return this.token;
	}
	public void setToken(OAuth2AccessToken token) {
		this.token = token;
	}
	public boolean isAuthenticated() {
		return this.token!=null;
	}
}
