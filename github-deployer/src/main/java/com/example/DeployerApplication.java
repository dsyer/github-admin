package com.example;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.cloud.deployer.resource.maven.MavenResourceLoader;
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeployerAutoConfiguration;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.util.ExecutorUtils;

@SpringBootApplication(exclude = CloudFoundryDeployerAutoConfiguration.class)
@EnableConfigurationProperties(ApplicationProperties.class)
@Controller
public class DeployerApplication {

	private static Logger log = LoggerFactory.getLogger(DeployerApplication.class);

	private static final String PREFIX = "github";

	private Map<String, String> props = Collections
			.singletonMap("spring.cloud.deployer.group", PREFIX);

	@Autowired
	private CloudFoundryOperations client;

	@Autowired
	private ApplicationProperties application;

	@Autowired
	private AppDeployer appDeployer;

	@Autowired
	private DelegatingResourceLoader delegatingResourceLoader;

	private ExecutorService executorService = ExecutorUtils.singleUse("deployer");

	@RequestMapping("/")
	public CompletableFuture<String> home(Map<String, Object> model) {
		Flux<ApplicationSummary> response = this.client.applications().list();
		return response.map(summary -> //
		{
			String name = summary.getName();
			String key = name.startsWith("github-") ? name.substring("github-".length())
					: name;
			return new Application(key, summary.getInstances(),
					summary.getRunningInstances(),
					this.application.getResources().get(key));
		}).toList()//
				.otherwise(e -> Mono.just(Collections.emptyList()))//
				.doOnSuccess(list -> model.put("apps", list))//
				.map(list -> "index")//
				.toCompletableFuture();
	}

	public CompletableFuture<String> update(@PathVariable String name) {
		String appId = PREFIX + "-" + name;
		String resource = this.application.getResources().get(name);
		Resource artifact = this.delegatingResourceLoader.getResource(resource);
		try {
			return this.client.applications()
					.push(PushApplicationRequest.builder().name(appId)
							.application(artifact.getInputStream()).build())
					.map(empty -> "redirect:/").toCompletableFuture();
		}
		catch (IOException e) {
			throw new IllegalStateException("Cannot update app", e);
		}
	}

	@PostMapping("/apps/{name}")
	public String push(@PathVariable String name) {
		log.info("Handling app deploy: " + name);
		this.executorService.submit(() -> {
			String resource = this.application.getResources().get(name);
			Resource artifact = this.delegatingResourceLoader.getResource(resource);
			String appId = PREFIX + "-" + name;
			try {
				log.info("Undeploying app: " + appId);
				this.appDeployer.undeploy(appId);
			}
			catch (IllegalStateException e) {
				log.info("Not deployed: " + appId);
				// Not deployed
			}
			int count = 0;
			DeploymentState state = this.appDeployer.status(appId).getState();
			while (state == DeploymentState.deployed && count++ <= 100) {
				try {
					Thread.sleep(500L);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
				log.info("Polling app: " + appId + ", " + state + " (" + count + ")");
			}
			if (count <= 100) {
				log.info("Deploying app: " + appId);
				this.appDeployer.deploy(new AppDeploymentRequest(
						new AppDefinition(name, null), artifact, this.props));
				return "OK";
			}
			return "NOK";
		});
		return "redirect:/";
	}

	@Bean
	@ConfigurationProperties
	public MavenProperties mavenProperties() {
		return new MavenProperties();
	}

	@Bean
	public MavenResourceLoader mavenResourceLoader(MavenProperties properties) {
		return new MavenResourceLoader(properties);
	}

	@Bean
	@ConditionalOnMissingBean(DelegatingResourceLoader.class)
	public DelegatingResourceLoader delegatingResourceLoader(
			MavenResourceLoader mavenResourceLoader) {
		Map<String, ResourceLoader> loaders = new HashMap<>();
		loaders.put("maven", mavenResourceLoader);
		ResourceLoader defaultResourceLoader = new DefaultResourceLoader();
		loaders.put("http", defaultResourceLoader);
		loaders.put("classpath", defaultResourceLoader);
		return new DelegatingResourceLoader(loaders);
	}

	public static void main(String[] args) {
		SpringApplication.run(DeployerApplication.class, args);
	}
}

class Application {
	String name;
	Integer instances;
	Integer runningInstances;
	String resource;

	public Application(String name, Integer instances, Integer runningInstances,
			String resource) {
		super();
		this.name = name;
		this.instances = instances;
		this.runningInstances = runningInstances;
		this.resource = resource;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getInstances() {
		return this.instances;
	}

	public void setInstances(Integer instances) {
		this.instances = instances;
	}

	public Integer getRunningInstances() {
		return this.runningInstances;
	}

	public void setRunningInstances(Integer runningInstances) {
		this.runningInstances = runningInstances;
	}

	public String getResource() {
		return this.resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

}
