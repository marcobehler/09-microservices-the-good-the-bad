package com.example.validationservice;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Thanks for watching this episode! Send any feedback to info@marcobehler.com!
 */
@Component
public class MyQueueListener {

    @JmsListener(destination = "my.queue")
    public void receiveMessage(String message) {
        System.out.println("received message = " + message);
    }
}
