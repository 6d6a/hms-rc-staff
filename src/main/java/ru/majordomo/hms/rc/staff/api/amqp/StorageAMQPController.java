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
import ru.majordomo.hms.rc.staff.managers.GovernorOfStorage;
import ru.majordomo.hms.rc.staff.resources.Storage;

@EnableRabbit
@Component
public class StorageAMQPController {
    private String applicationName;
    private Sender sender;
    private GovernorOfStorage governorOfStorage;
    private static final Logger logger = LoggerFactory.getLogger(StorageAMQPController.class);

    @Autowired
    public StorageAMQPController(Sender sender, GovernorOfStorage governorOfStorage) {
        this.sender = sender;
        this.governorOfStorage = governorOfStorage;
    }

    @Value("${spring.application.name}")
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "service.${spring.application.name}", durable = "true", autoDelete = "true"),
                exchange = @Exchange(value = "storage.create", type = "topic"),
                key = "service.${spring.application.name}"))
    public void create(@Payload ServiceMessage serviceMessage) {
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + "ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";
        ServiceMessage serviceReportMessage = new ServiceMessage();
        try {
            Storage storage = (Storage) governorOfStorage.createResource(serviceMessage);
            logger.info(loggerPrefix + "storage успешно создан");
            serviceReportMessage.setObjRef("http://" + applicationName + "/storage/" + storage.getId());
            serviceReportMessage.addParam("success", Boolean.TRUE);
        } catch (ParameterValidateException e) {
            logger.error(loggerPrefix + e.toString());
            serviceMessage.addParam("success", Boolean.FALSE);
        } finally {
            sender.send("storage.create", "service.pm", serviceReportMessage);
            logger.info(loggerPrefix + "отчет в pm отправлен");
        }
    }
}
