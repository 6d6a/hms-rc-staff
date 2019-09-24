package ru.majordomo.hms.rc.staff.resourceProcessor.support;

import ru.majordomo.hms.rc.staff.common.ResourceActionContext;
import ru.majordomo.hms.rc.staff.resources.Resource;

@FunctionalInterface
public interface ResultSender<T extends Resource> {
    void send(ResourceActionContext<T> context, String routingKey);
}
