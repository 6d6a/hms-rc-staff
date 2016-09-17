package ru.majordomo.hms.rc.staff.api.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;

@Component
public class Sender {
    @Autowired
    RabbitTemplate rabbitTemplate;

    private final static Logger logger = LoggerFactory.getLogger(Sender.class);

    public void send(String exchange, String routingKey, ServiceMessage payload) {
        Message message = MessageBuilder
                .withBody(payload.toJson().getBytes())
                .setContentType("application/json")
                .setHeader("provider","rc-user")
                .build();
        logger.warn(message.toString());
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
