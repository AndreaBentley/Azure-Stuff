package com.example.AzurePeekReceiveDemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class QueueReceiveController {

    private static final String QUEUE_NAME = "df-process-data";

    private final Logger logger = LoggerFactory.getLogger(QueueReceiveController.class);

//    Does not work, returns error:
//    2019-12-11 16:43:57.388  WARN 6700 --- [enerContainer-1] o.s.j.l.DefaultMessageListenerContainer  : Execution of JMS message listener failed, and no ErrorHandler has been set.
//
//    org.springframework.jms.listener.adapter.ListenerExecutionFailedException: Listener method 'public void com.example.AzurePeekReceiveDemo.QueueReceiveController.receiveMessage(com.example.AzurePeekReceiveDemo.User)' threw exception; nested exception is org.springframework.jms.support.converter.MessageConversionException: Could not convert JMS message; nested exception is javax.jms.MessageFormatException: Failed to read object
//    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
//    public void receiveMessage(User user) {
//        logger.info("Received message: {}", user.getName());
//    }

//    Works when handling Strings
    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(String message) {
        logger.info("Received message: {}", message);
    }

}
