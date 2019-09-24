package ru.majordomo.hms.rc.staff.resourceProcessor.impl.te;

import lombok.AllArgsConstructor;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.common.ResourceActionContext;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessor;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessorContext;
import ru.majordomo.hms.rc.staff.resources.Resource;

@AllArgsConstructor
public class TeDeleteProcessor<T extends Resource> implements ResourceProcessor<T> {
    private final ResourceProcessorContext<T> processorContext;

    @Override
    public void process(ResourceActionContext<T> context) {
        ServiceMessage serviceMessage = context.getMessage();

        Boolean successEvent = (Boolean) serviceMessage.getParam("success");

        String resourceUrl = serviceMessage.getObjRef();

        context.setResource(
                processorContext.getResourceByUrlBuilder().get(resourceUrl)
        );

        if (context.getResource() != null) {
            if (successEvent){
                processorContext.getGovernor().delete(context.getResource().getId());
            } else {
                processorContext.getGovernor().save(context.getResource());
            }
        }

        processorContext.getSender().send(
                context, processorContext.getRoutingKeyResolver().get(context)
        );
    }
}
