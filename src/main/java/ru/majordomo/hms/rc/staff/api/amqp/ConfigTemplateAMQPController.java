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
import ru.majordomo.hms.rc.staff.managers.GovernorOfConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

@EnableRabbit
@Component
public class ConfigTemplateAMQPController {
    private final Logger logger = LoggerFactory.getLogger(ConfigTemplateAMQPController.class);

    @Autowired
    GovernorOfConfigTemplate governorOfConfigTemplate;

    @Autowired
    Sender sender;

    @Value("${spring.application.name}")
    String applicationName;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "service.rc-staff", durable = "true", autoDelete = "true"),
            exchange = @Exchange(value = "ru.majordomo.hms.rc.staff.test.api.config-template.create", type = "topic"),
            key = "service.rc.staff"))
    public void create(@Payload ServiceMessage serviceMessage) {
        ServiceMessage reportServiceMessage = new ServiceMessage();
        reportServiceMessage.setOperationIdentity(serviceMessage.getOperationIdentity());
        reportServiceMessage.setActionIdentity(serviceMessage.getActionIdentity());
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + "ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";

        try {
            ConfigTemplate configTemplate = governorOfConfigTemplate.createResource(serviceMessage);
            reportServiceMessage.setObjRef("http://" + applicationName + "/ru.majordomo.hms.rc.staff.test.api.config-template/" + configTemplate.getId());
            reportServiceMessage.addParam("success", Boolean.TRUE);

        } catch (ParameterValidateException e) {
            reportServiceMessage.addParam("success", Boolean.FALSE);
        } finally {
            sender.send("ru.majordomo.hms.rc.staff.test.api.config-template.create", "service.pm", reportServiceMessage);
            logger.info(loggerPrefix + "Сообщение с отчетом отправлено");
        }
    }
}
