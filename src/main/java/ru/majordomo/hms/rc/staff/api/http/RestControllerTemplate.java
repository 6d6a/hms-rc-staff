package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.LordOfResources;
import ru.majordomo.hms.rc.staff.resources.Resource;

import java.util.Collection;

abstract public class RestControllerTemplate {

    protected LordOfResources governor;

    protected Resource processReadOneQuery(String resourceId) {
        return governor.build(resourceId);
    }

    protected Collection<? extends Resource> processReadAllQuery(String key) {
        return governor.buildAll(key);
    }

    protected ResponseEntity<?> processCreateQuery(Resource resource) throws ParameterValidateException {
        governor.isValid(resource);
        governor.save(resource);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(resource.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    protected ResponseEntity<?> processUpdateQuery(String resourceId, Resource resource) throws ParameterValidateException {
        governor.isValid(resource);
        Resource storedResource = governor.build(resourceId);
        resource.setId(storedResource.getId());
        governor.save(resource);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    protected ResponseEntity<?> processDeleteQuery(String resourceId) {
        Resource storedResource = governor.build(resourceId);
        governor.delete(resourceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
