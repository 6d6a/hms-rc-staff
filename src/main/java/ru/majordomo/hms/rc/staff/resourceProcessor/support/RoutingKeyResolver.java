package ru.majordomo.hms.rc.staff.resourceProcessor.support;

import ru.majordomo.hms.rc.staff.common.ResourceActionContext;
import ru.majordomo.hms.rc.staff.resources.Resource;

@FunctionalInterface
public interface RoutingKeyResolver<T extends Resource> {
    String get(ResourceActionContext<T> context);
}
