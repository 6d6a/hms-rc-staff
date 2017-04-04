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
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@EnableRabbit
@Component
public class ServiceTemplateAMQPController {
    private final Logger logger = LoggerFactory.getLogger(ServiceTemplateAMQPController.class);

    @Autowired
    GovernorOfServiceTemplate governorOfServiceTemplate;

    @Autowired
    Sender sender;

    @Value("${spring.application.name}")
    String applicationName;


    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "service.rc-staff", durable = "true", autoDelete = "true"),
            exchange = @Exchange(value = "service-template.create", type = "topic"),
            key = "service.rc.staff"))
    public void create(@Payload ServiceMessage serviceMessage) {
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + "ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";
        ServiceMessage serviceReportMessage = new ServiceMessage();

        try {
            ServiceTemplate serviceTemplate = governorOfServiceTemplate.createResource(serviceMessage);
            logger.info("service template успешно создан и сохранен");
            serviceReportMessage.setObjRef("http://" + applicationName + "/service-template/" + serviceTemplate.getId());
            serviceMessage.addParam("success", Boolean.TRUE);
        } catch (ParameterValidateException e) {
            serviceMessage.addParam("success", Boolean.FALSE);
        } finally {
            sender.send("service-template.create", "service.pm", serviceReportMessage);
            logger.info(loggerPrefix + "Сообщение с отчетом отправлено");
        }
    }
}
