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

import java.net.URL;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.CloudFoundryOperationsBuilder;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryAppDeployer;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeployerProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryTaskLauncher;
import org.springframework.cloud.deployer.spi.task.TaskLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Dave Syer
 *
 */
@Configuration
@EnableConfigurationProperties(CloudFoundryDeployerProperties.class)
public class CloudFoundryDeployerConfiguration {

	@Bean
	public CloudFoundryClient cloudFoundryClient(CloudFoundryDeployerProperties properties) {
		URL apiEndpoint = properties.getApiEndpoint();

		return SpringCloudFoundryClient.builder()
				.host(apiEndpoint.getHost())
				.port(apiEndpoint.getPort())
				.username(properties.getUsername())
				.password(properties.getPassword())
				.skipSslValidation(properties.isSkipSslValidation())
				.build();
	}

	@Bean
	@RefreshScope
	CloudFoundryOperations cloudFoundryOperations(CloudFoundryDeployerProperties properties, CloudFoundryClient cloudFoundryClient) {
		return new CloudFoundryOperationsBuilder()
				.cloudFoundryClient(cloudFoundryClient)
				.target(properties.getOrganization(), properties.getSpace())
				.build();
	}


	@Bean
	@ConditionalOnMissingBean(AppDeployer.class)
	public AppDeployer appDeployer(CloudFoundryDeployerProperties properties, CloudFoundryOperations operations) {
		return new CloudFoundryAppDeployer(properties, operations);
	}

	@Bean
	@ConditionalOnMissingBean(TaskLauncher.class)
	public TaskLauncher taskLauncher(CloudFoundryClient client) {
		return new CloudFoundryTaskLauncher(client);
	}
}
