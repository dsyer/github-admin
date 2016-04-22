package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

import de.codecentric.boot.admin.config.EnableAdminServer;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAdminServer
public class GithubAdminApplication {

	@Autowired
	private ApplicationEventPublisher publisher;

	@Scheduled(fixedDelay = 5000)
	public void update() {
		this.publisher.publishEvent(
				new InstanceRegisteredEvent<GithubAdminApplication>(this.publisher, this));
	}

	public static void main(String[] args) {
		SpringApplication.run(GithubAdminApplication.class, args);
	}
}
