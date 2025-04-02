package com.futurmaster.demandplanning.cucumber.utils;

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Session;

@TestConfiguration
public class BeanConfiguration {

    @Bean
    @Primary
    public SQSConnectionFactory connectionFactory() {
        return Mockito.mock(SQSConnectionFactory.class);
    }

    @Bean
    @Primary
    public DefaultJmsListenerContainerFactory sqsListenerContainerFactory(ActiveMQConnectionFactory activeMQConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(activeMQConnectionFactory);
        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        return factory;
    }

    @Bean
    public JmsTemplate defaultJmsTemplate(ActiveMQConnectionFactory activeMQConnectionFactory) {
        return new JmsTemplate(activeMQConnectionFactory);
    }

    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

}
