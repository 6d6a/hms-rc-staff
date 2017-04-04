package ru.majordomo.hms.rc.staff.managers;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;

import java.util.List;
import java.util.Map;

public abstract class LordOfResources<T extends Resource> {
    public abstract T createResource(ServiceMessage serviceMessage) throws ParameterValidateException;
    public abstract void isValid(T resource) throws ParameterValidateException;
    public abstract T build(String resourceId) throws ResourceNotFoundException;
    public abstract T build(Map<String, String> keyValue) throws ResourceNotFoundException;
    public abstract List<T> buildAll();
    public abstract List<T> buildAll(Map<String, String> keyValue);
    public abstract void save(T resource);
    public abstract void preDelete(String resourceId);
    public abstract void delete(String resourceId);
    public static Resource setResourceParams(Resource resource, ServiceMessage serviceMessage, Cleaner cleaner) throws ClassCastException{
        String name = cleaner.cleanString((String) serviceMessage.getParam("name"));
        resource.setName(name);

        Boolean switchedOn = (Boolean) serviceMessage.getParam("switchedOn");
        resource.setSwitchedOn(switchedOn);

        return resource;
    }
}
