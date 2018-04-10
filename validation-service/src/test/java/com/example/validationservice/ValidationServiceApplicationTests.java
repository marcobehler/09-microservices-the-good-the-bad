package com.example.validationservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ValidationServiceApplicationTests {

	@Autowired
	private JmsTemplate jmsTemplate;

	@Test
	public void contextLoads() throws InterruptedException {

		for (int i = 0; i < 10; i++) {
			Thread.sleep(1000);
			jmsTemplate.send("my.queue",
					session -> session.createTextMessage("Hello World!"));
		}
	}

}
