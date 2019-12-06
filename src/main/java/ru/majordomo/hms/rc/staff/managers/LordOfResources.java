package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ru.majordomo.hms.personmgr.exception.ParameterValidationException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.ResourceRepository;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;

import java.io.UnsupportedEncodingException;
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

    public T create(ServiceMessage serviceMessage) throws ParameterValidationException {
        T resource;

        try {
            resource = buildResourceFromServiceMessage(serviceMessage);
            preValidate(resource);
            isValid(resource);
            save(resource);
        } catch (ClassCastException | UnsupportedEncodingException e) {
            throw new ParameterValidationException("Один из параметров указан неверно:" + e.getMessage());
        }

        return resource;
    }

    public T buildResourceFromServiceMessage(ServiceMessage serviceMessage) throws ClassCastException, UnsupportedEncodingException {
        throw new NotImplementedException("Создание ресурса по AMQP не поддерживается");
    }

    public T update(ServiceMessage serviceMessage) throws ParameterValidationException, UnsupportedEncodingException {
        throw new NotImplementedException("Обновление ресурса по AMQP не поддерживается");
    }

    public abstract void isValid(T resource) throws ParameterValidateException;

    public T build(String resourceId) throws ResourceNotFoundException {
        T resource = repository.findById(resourceId).orElse(null);
        if (resource == null) {
            throw new ResourceNotFoundException(genericType.getSimpleName() +" с ID:" + resourceId + " не найден");
        }
        return resource;
    }

    public abstract T build(Map<String, String> keyValue) throws ResourceNotFoundException;

    public List<T> buildAll()  {
        return repository.findAll();
    }

    public List<T> buildAllOnlyIdAndName()  {
        return repository.findOnlyIdAndName();
    }

    public List<T> buildAllOnlyIdAndName(Map<String, String> keyValue)  {
        if (keyValue.get("name") != null) {
            return repository.findByNameOnlyIdAndName(keyValue.get("name"));
        } else {
            return repository.findOnlyIdAndName();
        }
    }

    public List<T> buildAll(Map<String, String> keyValue) {
        if (keyValue.get("name") != null) {
            if (keyValue.get("regex") != null) {
                return repository.findByNameRegEx(keyValue.get("name"));
            }
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
            if (keyValue.get("regex") != null) {
                return repository.findByNameRegEx(keyValue.get("name"), pageable);
            }
            return repository.findByName(keyValue.get("name"), pageable);
        } else {
            return repository.findAll(pageable);
        }
    }

    public void save(T resource) {
        repository.save(resource);
    }

    public void insert(T resource) {
        repository.insert(resource);
    }

    public void preDelete(String resourceId) {}

    public void delete(String resourceId) {
        preDelete(resourceId);
        repository.deleteById(resourceId);
    }

    public void preValidate(T resource) {}

    public void validateAndStore(T resource) {
        preValidate(resource);
        isValid(resource);
        save(resource);
    }

    public boolean exists(String resourceId) {
        return repository.existsById(resourceId);
    }

    public static Resource setResourceParams(Resource resource, ServiceMessage serviceMessage, Cleaner cleaner) throws ClassCastException{
        String name = cleaner.cleanString((String) serviceMessage.getParam("name"));
        resource.setName(name);

        Boolean switchedOn = (Boolean) serviceMessage.getParam("switchedOn");
        resource.setSwitchedOn(switchedOn);

        return resource;
    }
}
