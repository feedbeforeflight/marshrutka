package com.feedbeforeflight.marshrutka.rabbitmq;

import com.feedbeforeflight.marshrutka.transport.RabbitBrokerPoint;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

public class RabbitMessageReceiver {

    private final RabbitBrokerPoint rabbitBrokerPoint;

    public RabbitMessageReceiver(RabbitBrokerPoint rabbitBrokerPoint) {
        this.rabbitBrokerPoint = rabbitBrokerPoint;
    }

    public void receiveMessage(String message) {
        //System.out.println(message);
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/html; charset=utf-8");

        HttpEntity<String> request = new HttpEntity<>(message, headers);
        String result = restTemplate.postForObject(rabbitBrokerPoint.getReceiveURL(), request, String.class);

        //System.out.println(result);
    }

}
