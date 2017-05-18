package ru.majordomo.hms.rc.staff.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.ResourceRepository;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;

import java.util.List;
import java.util.Map;

public abstract class LordOfResources<T extends Resource> {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected ResourceRepository<T, String> repository;
    private final Class<T> genericType;

    @SuppressWarnings("unchecked")
    public LordOfResources() {
        this.genericType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), LordOfResources.class);
    }

    public abstract T createResource(ServiceMessage serviceMessage) throws ParameterValidateException;

    public abstract void isValid(T resource) throws ParameterValidateException;

    public T build(String resourceId) throws ResourceNotFoundException {
        T resource = repository.findOne(resourceId);
        if (resource == null) {
            throw new ResourceNotFoundException(genericType.getSimpleName() +" с ID:" + resourceId + " не найден");
        }
        return resource;
    }

    public abstract T build(Map<String, String> keyValue) throws ResourceNotFoundException;

    public List<T> buildAll()  {
        return repository.findAll();
    }

    public List<T> buildAll(Map<String, String> keyValue) {
        if (keyValue.get("name") != null) {
            return repository.findByName(keyValue.get("name"));
        } else {
            return repository.findAll();
        }
    }

    public Page<T> buildAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<T> buildAll(Map<String, String> keyValue, Pageable pageable) {
        if (keyValue.get("name") != null) {
            return repository.findByName(keyValue.get("name"), pageable);
        } else {
            return repository.findAll(pageable);
        }
    }

    public void save(T resource) {
        repository.save(resource);
    }

    public void preDelete(String resourceId) {}

    public void delete(String resourceId) {
        preDelete(resourceId);
        repository.delete(resourceId);
    }

    public void preValidate(T resource) {}

    public void validateAndStore(T resource) {
        preValidate(resource);
        isValid(resource);
        save(resource);
    }

    public boolean exists(String resourceId) {
        return repository.exists(resourceId);
    }

    public static Resource setResourceParams(Resource resource, ServiceMessage serviceMessage, Cleaner cleaner) throws ClassCastException{
        String name = cleaner.cleanString((String) serviceMessage.getParam("name"));
        resource.setName(name);

        Boolean switchedOn = (Boolean) serviceMessage.getParam("switchedOn");
        resource.setSwitchedOn(switchedOn);

        return resource;
    }
}
