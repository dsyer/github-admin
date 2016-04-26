package com.example;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DeployerApplication.class)
@WebIntegrationTest(randomPort=true)
public class DeployerApplicationTests {

	@Test
	public void contextLoads() throws InterruptedException, IOException {
	}

}
