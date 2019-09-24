package ru.majordomo.hms.rc.staff.resourceProcessor.impl;

import lombok.AllArgsConstructor;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.common.ResourceActionContext;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessor;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessorContext;
import ru.majordomo.hms.rc.staff.resources.ConnectableToServer;
import ru.majordomo.hms.rc.staff.resources.Resource;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class DefaultDeletePmProcessor<T extends Resource> implements ResourceProcessor<T> {
    private final ResourceProcessorContext<T> processorContext;

    @Override
    public void process(ResourceActionContext<T> context) throws Exception {
        ServiceMessage serviceMessage = context.getMessage();

        final String accountId = serviceMessage.getAccountId();

        String resourceId = null;


        if (serviceMessage.getParam("resourceId") != null) {
            resourceId = serviceMessage.getParam("resourceId").toString();
        }

        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("accountId", accountId);
        keyValue.put("resourceId", resourceId);

        T resource = processorContext.getGovernor().build(keyValue);

        context.setResource(resource);

        String routingKey = processorContext.getRoutingKeyResolver().get(context);

        if (resource instanceof ConnectableToServer) {
            processorContext.getGovernor().preDelete(resource.getId());
            processorContext.getGovernor().save(resource);
        } else {
            processorContext.getGovernor().delete(resourceId);
        }

        processorContext.getSender().send(context, routingKey);
    }
}
