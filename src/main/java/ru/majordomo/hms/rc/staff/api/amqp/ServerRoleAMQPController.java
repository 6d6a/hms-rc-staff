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
import ru.majordomo.hms.rc.staff.managers.GovernorOfServerRole;
import ru.majordomo.hms.rc.staff.resources.ServerRole;

@Component
@EnableRabbit
public class ServerRoleAMQPController {
    private GovernorOfServerRole governor;
    private Sender sender;
    private String applicationName;
    private String serviceName = "server-role";

    @Autowired
    public void setGovernor(GovernorOfServerRole governor) {
        this.governor = governor;
    }

    @Autowired
    public void setSender(Sender sender) {
        this.sender = sender;
    }

    @Value("${spring.application.name}")
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    private static final Logger logger = LoggerFactory.getLogger(ServerRoleAMQPController.class);

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "service.${spring.application.name}", durable = "true", autoDelete = "true"),
            exchange = @Exchange(value = "server-role.create", type = "topic"),
            key = "service.${spring.application.name}"))
    public void create(@Payload ServiceMessage serviceMessage) {
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + "ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";
        ServiceMessage reportServiceMessage = new ServiceMessage();
        reportServiceMessage.setActionIdentity(serviceMessage.getActionIdentity());
        reportServiceMessage.setOperationIdentity(serviceMessage.getOperationIdentity());
        try {
            ServerRole serverRole = (ServerRole) governor.createResource(serviceMessage);
            logger.info(loggerPrefix + "server role успешно создана");
            reportServiceMessage.setObjRef("http://" + applicationName + "/" + serviceName + "/" + serverRole.getId());
            reportServiceMessage.addParam("success", Boolean.TRUE);
        } catch (ParameterValidateException e) {
            logger.error(loggerPrefix + e.toString());
            reportServiceMessage.addParam("success", Boolean.FALSE);
        } finally {
            sender.send(serviceName + ".create", "service.pm", reportServiceMessage);
            logger.info(loggerPrefix + "отчет в pm отправлен");
        }
    }
}
