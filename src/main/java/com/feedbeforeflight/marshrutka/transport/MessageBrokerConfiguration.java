package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MessageBrokerConfiguration {

    @Bean
    MessageBroker messageBroker(PointRepository pointRepository, AmqpTemplate amqpTemplate, AmqpAdmin amqpAdmin) {
        return new RabbitMessageBroker(pointRepository, amqpTemplate, amqpAdmin);
    }

    @Bean
    MessageBrokerRepository messageBrokerRepository(MessageBroker messageBroker) {
        return messageBroker;
    }

    @Bean
    MessageBrokerManager messageBrokerManager(MessageBroker messageBroker) {
        return (MessageBrokerManager) messageBroker;
    }

    @Bean
    @Scope("prototype")
    BrokerPoint brokerPoint(AmqpTemplate amqpTemplate, AmqpAdmin amqpAdmin) {
        return new RabbitBrokerPoint(amqpTemplate, amqpAdmin);
    }

}
