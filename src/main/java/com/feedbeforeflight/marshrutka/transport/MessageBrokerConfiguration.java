package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
