package ru.majordomo.hms.rc.staff.resourceProcessor;

import ru.majordomo.hms.rc.staff.managers.LordOfResources;
import ru.majordomo.hms.rc.staff.resourceProcessor.support.ResourceByUrlBuilder;
import ru.majordomo.hms.rc.staff.resourceProcessor.support.ResultSender;
import ru.majordomo.hms.rc.staff.resourceProcessor.support.RoutingKeyResolver;
import ru.majordomo.hms.rc.staff.resources.Resource;

public interface ResourceProcessorContext<T extends Resource> {
    LordOfResources<T> getGovernor();
    ResultSender<T> getSender();
    RoutingKeyResolver<T> getRoutingKeyResolver();
    ResourceByUrlBuilder<T> getResourceByUrlBuilder();
}
