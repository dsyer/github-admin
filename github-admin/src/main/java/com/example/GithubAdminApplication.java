package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;

import de.codecentric.boot.admin.config.EnableAdminServer;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAdminServer
public class GithubAdminApplication extends AbstractEndpoint<String> {

	@Autowired
	private ApplicationEventPublisher publisher;

	public GithubAdminApplication() {
		super("update");
	}

	@Override
	public String invoke() {
		this.publisher
				.publishEvent(new InstanceRegisteredEvent<GithubAdminApplication>(this.publisher, this));
		return "OK";
	}

	public static void main(String[] args) {
		SpringApplication.run(GithubAdminApplication.class, args);
	}
}
