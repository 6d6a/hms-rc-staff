package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceTemplate;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@RestController
@CrossOrigin
@RequestMapping("/${spring.application.name}/service-template")
public class ServiceTemplateRestController {

    ServiceTemplateRepository repository;
    GovernorOfServiceTemplate governor;

    @Autowired
    public void setGovernor(GovernorOfServiceTemplate governor) {
        this.governor = governor;
    }

    @Autowired
    public void setRepository(ServiceTemplateRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "{serviceTemplateId}", method = RequestMethod.GET)
    public ServiceTemplate readOne(@PathVariable String serviceTemplateId) {
        return repository.findOne(serviceTemplateId);
    }

    @RequestMapping(value = {"/",""}, method = RequestMethod.GET)
    public Collection<ServiceTemplate> readAll() {
        return repository.findAll();
    }

    @RequestMapping(value = {"", ""}, method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody ServiceTemplate serviceTemplate) throws ParameterValidateException {
        governor.isValid(serviceTemplate);
        ServiceTemplate createdServiceTemplate = repository.save(serviceTemplate);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdServiceTemplate.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{serviceTemplateId}", method = RequestMethod.PATCH)
    public ResponseEntity<?> update(@PathVariable String serviceTemplateId,
                                    @RequestBody ServiceTemplate serviceTemplate) throws ParameterValidateException {
        governor.isValid(serviceTemplate);
        ServiceTemplate storedServiceTemplate = repository.findOne(serviceTemplateId);
        if (storedServiceTemplate == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        serviceTemplate.setId(storedServiceTemplate.getId());
        repository.save(serviceTemplate);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{serviceTemplateId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String serviceTemplateId) {
        ServiceTemplate storedServiceTemplate = repository.findOne(serviceTemplateId);
        if (storedServiceTemplate == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        repository.delete(serviceTemplateId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
