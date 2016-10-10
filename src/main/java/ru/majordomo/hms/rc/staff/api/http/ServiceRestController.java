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
import java.util.List;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfService;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@RestController
@RequestMapping("/${spring.application.name}/service")
public class ServiceRestController {

    private ServiceRepository repository;
    private GovernorOfService governor;
    private ServiceTemplateRepository templateRepository;
    private ServiceSocketRepository socketRepository;

    @Autowired
    public void setTemplateRepository(ServiceTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Autowired
    public void setSocketRepository(ServiceSocketRepository socketRepository) {
        this.socketRepository = socketRepository;
    }

    @Autowired
    public void setGovernor(GovernorOfService governor) {
        this.governor = governor;
    }

    @Autowired
    public void setRepository(ServiceRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/{serviceId}", method = RequestMethod.GET)
    public Service readOne(@PathVariable String serviceId) {
        return repository.findOne(serviceId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<Service> readAll() {
        return repository.findAll();
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody Service service) throws ParameterValidateException {
        governor.setServiceTemplateById(service, service.getServiceTemplateId());
        governor.setServiceSocketsByIds(service, service.getServiceSocketIds());
        governor.isValid(service);
        Service createdService = repository.save(service);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdService.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{serviceId}", method = RequestMethod.PATCH)
    public ResponseEntity<?> update(@PathVariable String serviceId,
                                    @RequestBody Service service) throws ParameterValidateException {
        governor.setServiceTemplateById(service, service.getServiceTemplateId());
        governor.setServiceSocketsByIds(service, service.getServiceSocketIds());
        governor.isValid(service);
        Service storedService = repository.findOne(serviceId);
        if (storedService == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        service.setId(storedService.getId());
        repository.save(service);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{serviceId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String serviceId) throws ParameterValidateException {
        Service storedService = repository.findOne(serviceId);
        if (storedService == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        repository.delete(serviceId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
