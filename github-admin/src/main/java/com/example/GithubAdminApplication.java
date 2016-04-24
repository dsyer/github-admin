package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import de.codecentric.boot.admin.config.EnableAdminServer;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAdminServer
@EnableAsync
public class GithubAdminApplication extends AbstractEndpoint<String> {

	public GithubAdminApplication() {
		super("update");
	}

	@Autowired
	private Service service;

	@Override
	public String invoke() {
		this.service.update();
		return "OK";
	}

	public static void main(String[] args) {
		SpringApplication.run(GithubAdminApplication.class, args);
	}
}

@Component
class Service {

	@Autowired
	private ApplicationEventPublisher publisher;

	@Async
	public void update() {
		this.publisher
				.publishEvent(new InstanceRegisteredEvent<Service>(this.publisher, this));
	}

}