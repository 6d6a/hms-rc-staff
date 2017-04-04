package ru.majordomo.hms.rc.staff.api.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfNetwork;
import ru.majordomo.hms.rc.staff.resources.Network;

@EnableRabbit
@Component
public class NetworkAMQPController {
    @Autowired
    GovernorOfNetwork governorOfNetwork;

    @Autowired
    Sender sender;

    @Value("${spring.application.name}")
    String applicationName;

    private final Logger logger = LoggerFactory.getLogger(NetworkAMQPController.class);

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "service.rc-staff", durable = "true", autoDelete = "true"),
            exchange = @Exchange(value = "network.create", type = "topic"),
            key = "service.rc.staff"))
    public void create(@Payload ServiceMessage serviceMessage) {
        ServiceMessage reportServiceMessage = new ServiceMessage();
        reportServiceMessage.setActionIdentity(serviceMessage.getActionIdentity());
        reportServiceMessage.setOperationIdentity(serviceMessage.getOperationIdentity());
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + "ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";
        try {
            Network network = governorOfNetwork.createResource(serviceMessage);
            logger.info(loggerPrefix + "ресурс network создан");
            reportServiceMessage.addParam("success", Boolean.TRUE);
        } catch (ParameterValidateException e) {
            logger.error(loggerPrefix + "Ошибка при создании ресурса network:" + e.getMessage());
            reportServiceMessage.addParam("success", Boolean.FALSE);
        } finally {
            sender.send("network.create","service.pm",reportServiceMessage);
        }
    }
}
