package ru.majordomo.hms.rc.staff.resourceProcessor.impl.te;

import lombok.AllArgsConstructor;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.common.MessageKeys;
import ru.majordomo.hms.rc.staff.common.ResourceActionContext;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessor;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessorContext;
import ru.majordomo.hms.rc.staff.resources.Resource;

@AllArgsConstructor
public class TeUpdateProcessor<T extends Resource> implements ResourceProcessor<T> {
    private final ResourceProcessorContext<T> processorContext;

    @Override
    public void process(ResourceActionContext<T> context) throws Exception {
        ServiceMessage serviceMessage = context.getMessage();

        String resourceUrl = serviceMessage.getObjRef();
        T resource = processorContext.getResourceByUrlBuilder().get(resourceUrl);

        context.setResource(resource);

        if (resource != null) {
            processorContext.getGovernor().save(resource);
        }

        //todo снять блокировку с ресурса
        if (!Boolean.TRUE.equals(serviceMessage.getParam(MessageKeys.STAFF_UPDATE))) {
            processorContext.getSender().send(context, processorContext.getRoutingKeyResolver().get(context));
        }
    }
}
