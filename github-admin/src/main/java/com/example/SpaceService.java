/*
 * Copyright 2012-2015 the original author or authors.
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
import java.net.URL;
import java.util.Collection;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.discovery.CloudFoundryDiscoveryProperties;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.stereotype.Service;

/**
 * @author Dave Syer
 *
 */
@Service
public class SpaceService {

	private CloudFoundryDiscoveryProperties discovery;

	private final OAuth2ClientContext credentials;

	@Autowired
	public SpaceService(CloudFoundryDiscoveryProperties discovery,
			OAuth2ClientContext credentials) {
		this.discovery = discovery;
		this.credentials = credentials;
	}

	public void choose(String org, String space) {
		URL url = getApiUrl();
		CloudFoundryClient cloudFoundryClient = new CloudFoundryClient(credentials(), url,
				org, space);
		
		credentials.setAccessToken(cloudFoundryClient.login());
	}

	public Collection<CloudSpace> spaces() {
		URL url = getApiUrl();
		CloudFoundryClient cloudFoundryClient = new CloudFoundryClient(credentials(), url);
		return cloudFoundryClient.getSpaces();
	}

	private CloudCredentials credentials() {
		return new CloudCredentials(credentials.getAccessToken());
	}

	private URL getApiUrl() {
		URL url;
		try {
			url = URI.create(this.discovery.getUrl()).toURL();
		}
		catch (MalformedURLException e) {
			throw new IllegalStateException("Cannot create URI", e);
		}
		return url;
	}

}
