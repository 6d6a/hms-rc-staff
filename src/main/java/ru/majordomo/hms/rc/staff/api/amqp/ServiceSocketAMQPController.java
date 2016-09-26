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
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceSocket;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

@EnableRabbit
@Component
public class ServiceSocketAMQPController {

    private String applicationName;
    private Sender sender;
    private GovernorOfServiceSocket governorOfServiceSocket;
    private static final Logger logger = LoggerFactory.getLogger(ServiceSocketAMQPController.class);

    @Autowired
    public ServiceSocketAMQPController(Sender sender, GovernorOfServiceSocket governorOfServiceSocket) {
        this.sender = sender;
        this.governorOfServiceSocket = governorOfServiceSocket;
    }

    @Value("${spring.application.name}")
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "service.${spring.application.name}", durable = "true", autoDelete = "true"),
            exchange = @Exchange(value = "service-socket.create", type = "topic"),
            key = "service.rc.staff"))
    public void create(@Payload ServiceMessage serviceMessage) {
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + "ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";
        ServiceMessage serviceReportMessage = new ServiceMessage();
        try {
            ServiceSocket serviceSocket = (ServiceSocket) governorOfServiceSocket.createResource(serviceMessage);
            logger.info(loggerPrefix + "service socket успешно создан и сохранен");
            serviceReportMessage.setObjRef("http://" + applicationName + "/service-socket/" + serviceSocket.getId());
            serviceReportMessage.addParam("success", Boolean.TRUE);
        } catch (ParameterValidateException e) {
            logger.error(loggerPrefix + e.toString());
            serviceMessage.addParam("success", Boolean.FALSE);
        } finally {
            sender.send("service-socket.create", "service.pm", serviceReportMessage);
            logger.info(loggerPrefix + "отчет в pm отправлен");
        }
    }
}
