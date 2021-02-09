package ru.majordomo.hms.rc.staff.api.clients;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;

import static ru.majordomo.hms.rc.staff.common.Constants.TE;

@Service
@RequiredArgsConstructor
public class Sender {

    private final static Logger logger = LoggerFactory.getLogger(Sender.class);

    private final RabbitTemplate rabbitTemplate;
    @Value("${hms.instance.name}")
    private final String instanceName;
    @Value("${hms.instance.name}.${spring.application.name}")
    private final String fullApplicationName;

    public void send(String exchange, String routingKey, ServiceMessage payload) {
        send(exchange, routingKey, payload, fullApplicationName);
    }

    public void send(String exchange, String routingKey, ServiceMessage payload, String provider) {
        if (!routingKey.startsWith(instanceName + ".")
                && !routingKey.startsWith(TE)
                && !routingKey.startsWith(instanceName + "." + TE)) {
            routingKey = instanceName + "." + routingKey;
        }

        if (!provider.startsWith(instanceName + ".")) {
            provider = instanceName + "." + provider;
        }

        Message message = buildMessage(payload, provider);
        rabbitTemplate.send(exchange, routingKey, message);
        logger.info("ACTION_IDENTITY: " + payload.getActionIdentity() +
                " OPERATION_IDENTITY: " + payload.getOperationIdentity() +
                " Сообщение от: " + provider + " " +
                "в exchange: " + exchange + " " +
                "с routing key: " + routingKey + " " +
                "отправлено." + " " +
                "Вот оно: " + message.toString());
    }

    private Message buildMessage(ServiceMessage payload, String provider) {
        return MessageBuilder
                .withBody(payload.toJson().getBytes())
                .setContentType("application/json")
                .setHeader("provider", provider)
                .build();
    }
}
