package ru.majordomo.hms.rc.staff.resourceProcessor.impl;

import lombok.AllArgsConstructor;
import ru.majordomo.hms.personmgr.exception.ParameterValidationException;
import ru.majordomo.hms.personmgr.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.common.ResourceActionContext;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessor;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessorContext;
import ru.majordomo.hms.rc.staff.resources.ConnectableToServer;
import ru.majordomo.hms.rc.staff.resources.Resource;

@AllArgsConstructor
public class DefaultUpdatePmProcessor<T extends Resource> implements ResourceProcessor<T> {
    private final ResourceProcessorContext<T> processorContext;

    @Override
    public void process(ResourceActionContext<T> context) throws Exception {
        ServiceMessage serviceMessage = context.getMessage();
        T resource;

        String resourceId = (String) serviceMessage.getParam("resourceId");
        if (resourceId == null || resourceId.equals("")) {
            throw new ParameterValidationException("Не указан resourceId");
        }
        try {
            processorContext.getGovernor().build(resourceId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Не найден ресурс с ID: " + resourceId);
        }

        resource = processorContext.getGovernor().update(serviceMessage);

        context.setResource(resource);

        String routingKey = processorContext.getRoutingKeyResolver().get(context);

        if (resource instanceof ConnectableToServer) {
            processorContext.getGovernor().save(resource);
        }

        processorContext.getSender().send(context, routingKey);
    }
}
