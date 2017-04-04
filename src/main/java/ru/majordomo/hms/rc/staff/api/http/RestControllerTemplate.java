package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.LordOfResources;
import ru.majordomo.hms.rc.staff.resources.Resource;

import java.util.Collection;
import java.util.Map;

abstract public class RestControllerTemplate<T extends Resource> {

    protected LordOfResources<T> governor;

    protected T processReadOneQuery(String resourceId) {
        return governor.build(resourceId);
    }

    protected Collection<T> processReadAllQuery() {
        return governor.buildAll();
    }

    protected Collection<T> processReadAllWithParamsQuery(Map<String, String> keyValue) {
        return governor.buildAll(keyValue);
    }

    protected T processReadOneWithParamsQuery(Map<String, String> keyValue) {
        return governor.build(keyValue);
    }

    protected ResponseEntity<T> processCreateQuery(T resource) throws ParameterValidateException {
        governor.isValid(resource);
        governor.save(resource);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(resource.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    protected ResponseEntity<T> processUpdateQuery(String resourceId, T resource) throws ParameterValidateException {
        governor.isValid(resource);
        Resource storedResource = governor.build(resourceId);
        resource.setId(storedResource.getId());
        governor.save(resource);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    protected ResponseEntity<Void> processDeleteQuery(String resourceId) {
        Resource storedResource = governor.build(resourceId);
        governor.delete(resourceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
