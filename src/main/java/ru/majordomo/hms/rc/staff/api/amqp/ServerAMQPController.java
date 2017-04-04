package ru.majordomo.hms.rc.staff.api.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServer;
import ru.majordomo.hms.rc.staff.resources.Server;

@Component
@EnableRabbit
public class ServerAMQPController {
    private GovernorOfServer governor;
    private Sender sender;
    private String applicationName;
    private String serviceName = "service";

    @Autowired
    public void setSender(Sender sender) {
        this.sender = sender;
    }

    @Value("${spring.application.name}")
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Autowired
    public void setGovernor(GovernorOfServer governor) {
        this.governor = governor;
    }

    private static final Logger logger = LoggerFactory.getLogger(ServerAMQPController.class);
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "service.${spring.application.name}", durable = "true", autoDelete = "true"),
            exchange = @Exchange(value = "server.create", type = "topic"),
            key = "server.${spring.application.name}"))
    public void create(@Payload ServiceMessage serviceMessage) {
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + "ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";
        ServiceMessage reportServiceMessage = new ServiceMessage();
        reportServiceMessage.setActionIdentity(serviceMessage.getActionIdentity());
        reportServiceMessage.setOperationIdentity(serviceMessage.getOperationIdentity());
        try {
            Server server = governor.createResource(serviceMessage);
            logger.info(loggerPrefix + "service успешно создан");
            reportServiceMessage.setObjRef("http://" + applicationName + "/" + serviceName + "/" + server.getId());
            reportServiceMessage.addParam("success", Boolean.TRUE);
        } catch (ParameterValidateException e) {
            logger.error(loggerPrefix + e.toString());
            reportServiceMessage.addParam("success", Boolean.FALSE);
        } finally {
            sender.send(serviceName + ".create", "server.pm", reportServiceMessage);
            logger.info(loggerPrefix + "отчет в pm отправлен");
        }
    }

}
