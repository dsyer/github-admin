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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.discovery.CloudFoundryDiscoveryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.oauth2.client.OAuth2ClientContext;

/**
 * @author Dave Syer
 *
 */
@Configuration
public class CloudFoundryClientConfiguration {

	private static Logger logger = LoggerFactory
			.getLogger(CloudFoundryClientConfiguration.class);

	@Autowired
	private CloudFoundryDiscoveryProperties discovery;

	@Bean
	@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
	@Lazy
	public CloudFoundryClient cloudFoundryClient(OAuth2ClientContext context)
			throws MalformedURLException {
		CloudFoundryClient cloudFoundryClient;
		CloudCredentials credentials = new CloudCredentials(context.getAccessToken());
		logger.info("Discovery set up for: " + discovery.getUrl() + " ["
				+ discovery.getOrg() + "," + discovery.getSpace() + "]");
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
