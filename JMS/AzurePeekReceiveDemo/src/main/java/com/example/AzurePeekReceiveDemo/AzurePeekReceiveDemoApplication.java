package com.example.AzurePeekReceiveDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableJms
@SpringBootApplication
public class AzurePeekReceiveDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AzurePeekReceiveDemoApplication.class, args);
	}


//	If I attempt to create my own converter for the receiver, I get this error message:
//	2019-12-11 16:45:59.114  WARN 16532 --- [enerContainer-2] o.s.j.l.DefaultMessageListenerContainer  : Setup of JMS message listener invoker failed for destination 'df-process-data' - trying to recover. Cause: Transaction is not supported because the namespace 'avantarde-poc-bus' is of 'Basic' tier. [condition = amqp:not-allowed]
//	2019-12-11 16:45:59.223  WARN 16532 --- [windows.net:-1]] o.a.q.j.p.a.b.AmqpResourceBuilder        : Open of resource:(JmsSessionInfo { ID:48d33819-9ef5-4fda-ae8c-82613777c87d:1:3 }) failed: Transaction is not supported because the namespace 'avantarde-poc-bus' is of 'Basic' tier. [condition = amqp:not-allowed]

//	@Bean
//	public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
//													DefaultJmsListenerContainerFactoryConfigurer configurer) {
//		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//		// This provides all boot's default to this factory, including the message converter
//		configurer.configure(factory, connectionFactory);
//		// You could still override some of Boot's default if necessary.
//		return factory;
//	}
//
//	@Bean // Serialize message content to json using TextMessage
//	public MappingJackson2MessageConverter jacksonJmsMessageConverter() {
//		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
//		converter.setTargetType(MessageType.TEXT);
//		converter.setTypeIdPropertyName("_type");
//		return converter;
//	}


}
