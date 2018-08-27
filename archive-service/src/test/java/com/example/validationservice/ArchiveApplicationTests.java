package com.example.validationservice;

import com.marcobehler.microservices.BankStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.ObjectMessage;
import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ArchiveApplication.class)
public class ArchiveApplicationTests {

	@Autowired
	private JmsTemplate jmsTemplate;

	@Test
	public void contextLoads() throws InterruptedException {

		for (int i = 0; i < 10; i++) {


			Thread.sleep(1000);
			jmsTemplate.send("archive.queue",
					session -> {
						ObjectMessage message = session.createObjectMessage();

						BankStatement bankStatement = new BankStatement(false, "this is an error message", "<xml2></xml>");
						ArrayList<Object> list = new ArrayList<>();
						list.add(bankStatement);

						message.setObject(list);
						return message;
					});
		}
	}

}
