package ru.majordomo.hms.rc.staff.resourceProcessor;

import ru.majordomo.hms.rc.staff.common.ResourceActionContext;
import ru.majordomo.hms.rc.staff.resources.Resource;

@FunctionalInterface
public interface ResourceProcessor<T extends Resource> {
    void process(ResourceActionContext<T> context) throws Exception;
}
