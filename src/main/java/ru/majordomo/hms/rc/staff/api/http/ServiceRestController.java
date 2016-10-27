package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfService;
import ru.majordomo.hms.rc.staff.resources.Service;

@RestController
@RequestMapping("/${spring.application.name}/service")
public class ServiceRestController {

    private GovernorOfService governor;

    @Autowired
    public void setGovernor(GovernorOfService governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{serviceId}", method = RequestMethod.GET)
    public Service readOne(@PathVariable String serviceId) {
        return (Service) governor.build(serviceId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<Service> readAll() {
        return governor.build();
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody Service service) throws ParameterValidateException {
        governor.isValid(service);
        Service createdService = (Service) governor.save(service);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdService.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{serviceId}", method = RequestMethod.PATCH)
    public ResponseEntity<?> update(@PathVariable String serviceId,
                                    @RequestBody Service service) throws ParameterValidateException {
        governor.isValid(service);
        try {
            Service storedService = (Service) governor.build(serviceId);
            service.setId(storedService.getId());
            governor.save(service);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{serviceId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String serviceId) throws ParameterValidateException {
        try {
            Service storedService = (Service) governor.build(serviceId);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        governor.delete(serviceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
