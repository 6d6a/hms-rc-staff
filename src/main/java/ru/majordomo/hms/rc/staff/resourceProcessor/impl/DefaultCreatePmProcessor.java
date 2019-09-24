package ru.majordomo.hms.rc.staff.resourceProcessor.impl;

import lombok.AllArgsConstructor;
import ru.majordomo.hms.rc.staff.common.ResourceActionContext;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessor;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessorContext;
import ru.majordomo.hms.rc.staff.resources.ConnectableToServer;
import ru.majordomo.hms.rc.staff.resources.Resource;

@AllArgsConstructor
public class DefaultCreatePmProcessor<T extends Resource> implements ResourceProcessor<T> {
    private final ResourceProcessorContext<T> processorContext;

    @Override
    public void process(ResourceActionContext<T> context) {
        T resource = processorContext.getGovernor().create(context.getMessage());

        context.setResource(resource);

        String routingKey = processorContext.getRoutingKeyResolver().get(context);

        if (resource instanceof ConnectableToServer) {
            processorContext.getGovernor().save(resource);
        }

        processorContext.getSender().send(context, routingKey);
    }
}
