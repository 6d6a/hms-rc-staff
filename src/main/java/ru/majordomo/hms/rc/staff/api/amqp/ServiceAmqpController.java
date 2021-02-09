package ru.majordomo.hms.rc.staff.api.amqp;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.common.ResourceActionContext;
import ru.majordomo.hms.rc.staff.managers.GovernorOfService;
import ru.majordomo.hms.rc.staff.resources.Service;

import javax.annotation.ParametersAreNonnullByDefault;

import static ru.majordomo.hms.rc.staff.config.RabbitMqConfig.Exchanges.*;
import static ru.majordomo.hms.rc.staff.common.Constants.PM;
import static ru.majordomo.hms.rc.staff.common.Constants.TE;

@Component
@ParametersAreNonnullByDefault
public class ServiceAmqpController extends BaseAmqpController<Service> {

    @Autowired
    public void setGovernor(GovernorOfService governor) {
        this.governor = governor;
    }

    @RabbitListener(queues = "${hms.instance.name}" + "." + "${spring.application.name}" + "." + SERVICE_CREATE)
    public void handleCreateEvent(
            Message amqpMessage,
            @Header(value = "provider") String eventProvider,
            @Payload ServiceMessage serviceMessage
    ) {
        handleCreate(eventProvider, serviceMessage);
    }

    @RabbitListener(queues = "${hms.instance.name}" + "." + "${spring.application.name}" + "." + SERVICE_UPDATE)
    public void handleUpdateEvent(
            Message amqpMessage,
            @Header(value = "provider") String eventProvider,
            @Payload ServiceMessage serviceMessage
    ) {
        handleUpdate(eventProvider, serviceMessage);
    }

    @RabbitListener(queues = "${hms.instance.name}" + "." + "${spring.application.name}" + "." + SERVICE_DELETE)
    public void handleDeleteEvent(
            Message amqpMessage,
            @Header(value = "provider") String eventProvider,
            @Payload ServiceMessage serviceMessage
    ) {
        handleDelete(eventProvider, serviceMessage);
    }

    @Override
    public String getResourceType() {
        return Resource.SERVICE;
    }

    @Override
    protected String getRoutingKey(ResourceActionContext<Service> context) {
        String routingKey = getDefaultRoutingKey();

        if (context.getEventProvider().equals(PM)) {
            routingKey = getTaskExecutorRoutingKey(context.getResource());
        } else if (context.getEventProvider().equals(TE)) {
            routingKey = getDefaultRoutingKey();
        }

        return routingKey;
    }
}
