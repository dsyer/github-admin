package com.example;

import java.util.List;
import java.util.Map;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import reactor.core.publisher.Flux;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
@Controller
public class DeployerApplication {

	@Autowired
	private CloudFoundryOperations client;

	@Autowired
	private ApplicationProperties application;

	@Autowired
	private AppDeployer appDeployer;

	@RequestMapping("/")
	public String home(Map<String, Object> model) {
		Flux<ApplicationSummary> response = this.client.applications().list();
		List<Application> list = response
				.map(summary -> new Application(summary.getName(), summary.getInstances(),
						summary.getRunningInstances(),
						this.application.getResources().get(summary.getName())))
				.toList().get();
		model.put("apps", list);
		return "index";
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