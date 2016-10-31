package ru.majordomo.hms.rc.staff.managers;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LordOfResources {
    public abstract Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException;
    public abstract void isValid(Resource resource) throws ParameterValidateException;
    public abstract Resource build(String resourceId) throws ResourceNotFoundException;
    public abstract List<? extends Resource> build();
    public abstract List<? extends Resource> build(Map<String, String> keyValue);
    public abstract void save(Resource resource);
    public abstract void delete(String resourceId);
    public static Resource setResourceParams(Resource resource, ServiceMessage serviceMessage, Cleaner cleaner) throws ClassCastException{
        String name = cleaner.cleanString((String) serviceMessage.getParam("name"));
        resource.setName(name);

        Boolean switchedOn = (Boolean) serviceMessage.getParam("switchedOn");
        resource.setSwitchedOn(switchedOn);

        return resource;
    }
}
